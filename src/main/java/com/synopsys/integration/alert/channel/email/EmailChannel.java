/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.NamedDistributionChannel;
import com.synopsys.integration.alert.common.channel.email.EmailMessagingService;
import com.synopsys.integration.alert.common.channel.email.EmailProperties;
import com.synopsys.integration.alert.common.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class EmailChannel extends NamedDistributionChannel {
    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String FILE_NAME_SYNOPSYS_LOGO = "synopsys.png";
    public static final String FILE_NAME_MESSAGE_TEMPLATE = "message_content.ftl";
    public static final String DIRECTORY_EMAIL_IMAGE_RESOURCES = "/src/main/resources/email/images/";

    private final EmailAddressHandler emailAddressHandler;
    private final FreemarkerTemplatingService freemarkerTemplatingService;
    private final AlertProperties alertProperties;
    private final EmailChannelMessageParser emailChannelMessageParser;

    @Autowired
    public EmailChannel(EmailChannelKey emailChannelKey, Gson gson, AlertProperties alertProperties, DefaultAuditUtility auditUtility,
        EmailAddressHandler emailAddressHandler, FreemarkerTemplatingService freemarkerTemplatingService, EmailChannelMessageParser emailChannelMessageParser) {
        super(emailChannelKey, gson, auditUtility);
        this.emailAddressHandler = emailAddressHandler;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        this.alertProperties = alertProperties;
        this.emailChannelMessageParser = emailChannelMessageParser;
    }

    @Override
    public void distributeMessage(DistributionEvent event) throws IntegrationException {
        FieldAccessor fieldAccessor = event.getFieldAccessor();

        Optional<String> host = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        Optional<String> from = fieldAccessor.getString(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());

        if (!host.isPresent() || !from.isPresent()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        // FIXME this should update addresses based on Provider event.getProvider()
        FieldAccessor updatedFieldAccessor = emailAddressHandler.updateEmailAddresses(event.getContent(), fieldAccessor);

        Set<String> emailAddresses = updatedFieldAccessor.getAllStrings(EmailDescriptor.KEY_EMAIL_ADDRESSES).stream().collect(Collectors.toSet());
        EmailProperties emailProperties = new EmailProperties(updatedFieldAccessor);
        String subjectLine = fieldAccessor.getStringOrEmpty(EmailDescriptor.KEY_SUBJECT_LINE);
        sendMessage(emailProperties, emailAddresses, subjectLine, event.getFormatType(), event.getContent());
    }

    public void sendMessage(EmailProperties emailProperties, Set<String> emailAddresses, String subjectLine, String formatType, MessageContentGroup messageContent) throws IntegrationException {
        String topicValue = null;
        if (!messageContent.isEmpty()) {
            topicValue = messageContent.getCommonTopic().getValue();
        }

        String alertServerUrl = alertProperties.getServerUrl().orElse(null);
        LinkableItem comonProvider = messageContent.getCommonProvider();
        String providerName = comonProvider.getValue();
        String providerUrl = comonProvider.getUrl().orElse("#");

        if (null == emailAddresses || emailAddresses.isEmpty()) {
            String errorMessage = String.format("ERROR: Could not determine what email addresses to send this content to. Provider: %s. Topic: %s", providerName, topicValue);
            throw new AlertException(errorMessage);
        }
        HashMap<String, Object> model = new HashMap<>();
        Map<String, String> contentIdsToFilePaths = new HashMap<>();

        String formattedContent = emailChannelMessageParser.createMessage(messageContent);

        model.put(EmailPropertyKeys.EMAIL_CONTENT.getPropertyKey(), formattedContent);
        model.put(EmailPropertyKeys.EMAIL_CATEGORY.getPropertyKey(), formatType);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey(), createEnhancedSubjectLine(subjectLine, topicValue));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_URL.getPropertyKey(), providerUrl);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_NAME.getPropertyKey(), providerName);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_PROVIDER_PROJECT_NAME.getPropertyKey(), topicValue);
        model.put(EmailPropertyKeys.TEMPLATE_KEY_START_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(EmailPropertyKeys.TEMPLATE_KEY_END_DATE.getPropertyKey(), String.valueOf(System.currentTimeMillis()));
        model.put(FreemarkerTemplatingService.KEY_ALERT_SERVER_URL, alertServerUrl);

        EmailMessagingService emailService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);
        emailService.addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), getImagePath(FILE_NAME_SYNOPSYS_LOGO));
        if (!model.isEmpty()) {
            EmailTarget emailTarget = new EmailTarget(emailAddresses, FILE_NAME_MESSAGE_TEMPLATE, model, contentIdsToFilePaths);
            emailService.sendEmailMessage(emailTarget);
        }
    }

    private String createEnhancedSubjectLine(String originalSubjectLine, String providerProjectName) {
        if (StringUtils.isNotBlank(providerProjectName)) {
            return String.format("%s | For: %s", originalSubjectLine, providerProjectName);
        }
        return originalSubjectLine;
    }

    private String getImagePath(String imageFileName) {
        String imagesDirectory = alertProperties.getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return imagesDirectory + "/" + imageFileName;
        }
        String userDirectory = System.getProperties().getProperty(PROPERTY_USER_DIR);
        return userDirectory + DIRECTORY_EMAIL_IMAGE_RESOURCES + imageFileName;
    }

}
