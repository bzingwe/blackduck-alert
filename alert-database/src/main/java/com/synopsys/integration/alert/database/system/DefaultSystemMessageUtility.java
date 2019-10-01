/**
 * alert-database
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
package com.synopsys.integration.alert.database.system;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.rest.model.SystemMessageModel;
import com.synopsys.integration.rest.RestConstants;

@Component
public class DefaultSystemMessageUtility implements SystemMessageUtility {
    private Logger logger = LoggerFactory.getLogger(DefaultSystemMessageUtility.class);
    private final SystemMessageRepository systemMessageRepository;

    @Autowired
    public DefaultSystemMessageUtility(final SystemMessageRepository systemMessageRepository) {
        this.systemMessageRepository = systemMessageRepository;
    }

    @Override
    @Transactional
    public void addSystemMessage(final String message, final SystemMessageSeverity severity, final SystemMessageType messageType) {
        final Date currentTime = DateRange.createCurrentDateTimestamp();
        final SystemMessage systemMessage = new SystemMessage(currentTime, severity.name(), message, messageType.name());
        systemMessageRepository.save(systemMessage);
    }

    @Override
    @Transactional
    public void removeSystemMessagesByType(final SystemMessageType messageType) {
        final List<SystemMessage> messages = systemMessageRepository.findByType(messageType.name());
        systemMessageRepository.deleteAll(messages);
    }

    @Override
    @Transactional
    public List<SystemMessageModel> getSystemMessages() {
        return convertAllToSystemMessageModel(systemMessageRepository.findAll());
    }

    @Override
    @Transactional
    public List<SystemMessageModel> getSystemMessagesAfter(final Date date) {
        final Date currentTime = DateRange.createCurrentDateTimestamp();
        return convertAllToSystemMessageModel(systemMessageRepository.findByCreatedBetween(date, currentTime));
    }

    @Override
    @Transactional
    public List<SystemMessageModel> getSystemMessagesBefore(final Date date) {
        final long recordCount = systemMessageRepository.count();
        if (recordCount == 0) {
            return Collections.emptyList();
        } else {
            final SystemMessage oldestMessage = systemMessageRepository.findTopByOrderByCreatedAsc();
            return convertAllToSystemMessageModel(systemMessageRepository.findByCreatedBetween(oldestMessage.getCreated(), date));
        }
    }

    @Override
    @Transactional
    public List<SystemMessageModel> findBetween(final DateRange dateRange) {
        return systemMessageRepository.findByCreatedBetween(dateRange.getStart(), dateRange.getEnd()).stream().map(this::convertToSystemMessageModel).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSystemMessages(final List<SystemMessageModel> messagesToDelete) {
        final List<SystemMessage> convertedMessages = messagesToDelete.stream()
                                                          .map(this::convertToSystemMessage)
                                                          .filter(message -> message != null).collect(Collectors.toList());
        systemMessageRepository.deleteAll(convertedMessages);
    }

    private List<SystemMessageModel> convertAllToSystemMessageModel(List<SystemMessage> systemMessages) {
        return systemMessages.stream().map(this::convertToSystemMessageModel).collect(Collectors.toList());
    }

    private SystemMessageModel convertToSystemMessageModel(final SystemMessage systemMessage) {
        final String createdAt = RestConstants.formatDate(systemMessage.getCreated());
        return new SystemMessageModel(systemMessage.getSeverity(), createdAt, systemMessage.getContent(), systemMessage.getType());
    }

    private SystemMessage convertToSystemMessage(SystemMessageModel systemMessageModel) {
        try {
            Date date = RestConstants.parseDateString(systemMessageModel.getCreatedAt());
            return new SystemMessage(date, systemMessageModel.getSeverity(), systemMessageModel.getContent(), systemMessageModel.getType());
        } catch (ParseException e) {
            logger.error("There was an issue parsing the stored CreatedAt date.");
        }
        return null;
    }

}
