/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.web.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.event.ChannelEventFactory;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.web.exception.AlertNotificationPurgedException;
import com.blackducksoftware.integration.alert.web.model.AlertPagedModel;
import com.blackducksoftware.integration.alert.web.model.ComponentConfig;
import com.blackducksoftware.integration.alert.web.model.NotificationContentConverter;
import com.blackducksoftware.integration.alert.web.model.NotificationConfig;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.blackducksoftware.integration.exception.IntegrationException;

@Transactional
@Component
public class AuditEntryActions {
    private final Logger logger = LoggerFactory.getLogger(AuditEntryActions.class);

    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final NotificationManager notificationManager;
    private final CommonDistributionRepository commonDistributionRepository;
    private final NotificationContentConverter notificationContentConverter;
    private final ChannelEventFactory channelEventFactory;
    private final ProjectDataFactory projectDataFactory;
    private final ChannelTemplateManager channelTemplateManager;

    @Autowired
    public AuditEntryActions(final AuditEntryRepository auditEntryRepository, final NotificationManager notificationManager, final AuditNotificationRepository auditNotificationRepository,
            final CommonDistributionRepository commonDistributionRepository, final NotificationContentConverter notificationContentConverter,
            final ChannelEventFactory channelEventFactory, final ProjectDataFactory projectDataFactory,
            final ChannelTemplateManager channelTemplateManager) {
        this.auditEntryRepository = auditEntryRepository;
        this.notificationManager = notificationManager;
        this.auditNotificationRepository = auditNotificationRepository;
        this.commonDistributionRepository = commonDistributionRepository;
        this.notificationContentConverter = notificationContentConverter;
        this.channelEventFactory = channelEventFactory;
        this.projectDataFactory = projectDataFactory;
        this.channelTemplateManager = channelTemplateManager;
    }

    public AlertPagedModel<AuditEntryRestModel> get() {
        return get(null, null);
    }

    public AlertPagedModel<AuditEntryRestModel> get(final Integer pageNumber, final Integer pageSize) {
        final List<AuditEntryEntity> auditEntries;
        logger.debug("Audit entry get. PageNumber: {} PageSize: {}", pageNumber, pageSize);
        int totalPages = 1;
        int pageNumberResponse = 0;
        if (pageNumber != null && pageSize != null) {
            final PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, new Sort(Sort.Direction.DESC, "timeLastSent"));
            final Page<AuditEntryEntity> page = auditEntryRepository.findAll(pageRequest);
            totalPages = page.getTotalPages();
            pageNumberResponse = page.getNumber();
            auditEntries = page.getContent();
        } else {
            final List<AuditEntryEntity> contentList = auditEntryRepository.findAll();
            auditEntries = contentList;
        }
        final List<AuditEntryRestModel> auditEntryRestModels = createRestModels(auditEntries);
        final AlertPagedModel<AuditEntryRestModel> pagedRestModel = new AlertPagedModel(totalPages, pageNumberResponse, auditEntryRestModels.size(), auditEntryRestModels);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    public AuditEntryRestModel get(final Long id) {
        if (id != null) {
            final Optional<AuditEntryEntity> auditEntryEntity = auditEntryRepository.findById(id);
            if (auditEntryEntity.isPresent()) {
                return createRestModel(auditEntryEntity.get());
            }
        }
        return null;
    }

    public AlertPagedModel<AuditEntryRestModel> search(final Integer pageNumber, final Integer pageSize, final String searchTerm) {
        final List<AuditEntryRestModel> auditEntries = new ArrayList<AuditEntryRestModel>();
        logger.debug("Audit entry search. PageNumber: {} PageSize: {} SearchTerm: {}", pageNumber, pageSize, searchTerm);
        final List<AuditEntryEntity> contentList = auditEntryRepository.findAll();
        final List<AuditEntryRestModel> currentPageRestModels = createRestModels(contentList);
        addMatchingModels(auditEntries, currentPageRestModels, searchTerm);

        List<AuditEntryRestModel> pagedAuditEntries = auditEntries;
        int totalPages = 1;
        int pageNumberResponse = 0;
        if (null != pageSize) {
            pagedAuditEntries = new ArrayList<AuditEntryRestModel>();
            final int pageStart = pageNumber * pageSize;
            final int pageEnd = pageStart + pageSize;
            for (int i = 0; i < auditEntries.size(); i++) {
                if (i >= pageStart && i < pageEnd) {
                    pageNumberResponse = pageNumber;
                    pagedAuditEntries.add(auditEntries.get(i));
                }
            }
            final int count = auditEntries.size();
            final double division = (double) count / (double) pageSize;
            final double ceiling = Math.ceil(division);
            totalPages = (int) Math.round(ceiling);
        }
        final AlertPagedModel<AuditEntryRestModel> pagedRestModel = new AlertPagedModel<AuditEntryRestModel>(totalPages, pageNumberResponse, pagedAuditEntries.size(), pagedAuditEntries);
        logger.debug("Paged Audit Entry Rest Model: {}", pagedRestModel);
        return pagedRestModel;
    }

    private void addMatchingModels(final List<AuditEntryRestModel> listToAddTo, final List<AuditEntryRestModel> modelsToCheck, final String searchTerm) {
        for (final AuditEntryRestModel restModel : modelsToCheck) {
            if (restModel.getName().contains(searchTerm) || restModel.getStatus().contains(searchTerm) || restModel.getTimeCreated().contains(searchTerm) || restModel.getTimeLastSent().contains(searchTerm)) {
                listToAddTo.add(restModel);
            } else if (null != restModel.getNotification() && restModel.getNotification().getProjectName().contains(searchTerm)) {
                listToAddTo.add(restModel);
            }
        }
    }

    public AlertPagedModel<AuditEntryRestModel> resendNotification(final Long id) throws IntegrationException {
        final Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(id);
        if (!auditEntryEntityOptional.isPresent()) {
            throw new AlertException("No audit entry with the provided id exists.");
        }

        final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.get();
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(AuditNotificationRelation::getNotificationId).collect(Collectors.toList());
        final List<NotificationModel> notifications = notificationManager.findByIds(notificationIds);
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();
        final Optional<CommonDistributionConfigEntity> commonConfigEntity = commonDistributionRepository.findById(commonConfigId);
        if (notifications == null || notifications.isEmpty()) {
            throw new AlertNotificationPurgedException("The notification for this entry was purged. To edit the purge schedule, please see the Scheduling Configuration.");
        }
        if (!commonConfigEntity.isPresent()) {
            throw new AlertException("The job for this entry was deleted, can not re-send this entry.");
        }
        final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(notifications);
        final DigestModel digestModel = new DigestModel(projectDataCollection);
        final ChannelEvent event = channelEventFactory.createEvent(commonConfigId, commonConfigEntity.get().getDistributionType(), digestModel);
        event.setAuditEntryId(auditEntryEntity.getId());
        channelTemplateManager.sendEvent(event);
        return get();
    }

    private List<AuditEntryRestModel> createRestModels(final List<AuditEntryEntity> auditEntryEntities) {
        return auditEntryEntities.stream().map(this::createRestModel).collect(Collectors.toList());
    }

    private AuditEntryRestModel createRestModel(final AuditEntryEntity auditEntryEntity) {
        final Long commonConfigId = auditEntryEntity.getCommonConfigId();

        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByAuditEntryId(auditEntryEntity.getId());
        final List<Long> notificationIds = relations.stream().map(AuditNotificationRelation::getNotificationId).collect(Collectors.toList());
        final List<NotificationModel> notifications = notificationManager.findByIds(notificationIds);

        final Optional<CommonDistributionConfigEntity> commonConfigEntity = commonDistributionRepository.findById(commonConfigId);

        final String id = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getId());
        final String timeCreated = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeCreated());
        final String timeLastSent = notificationContentConverter.getContentConverter().getStringValue(auditEntryEntity.getTimeLastSent());

        String status = null;
        if (auditEntryEntity.getStatus() != null) {
            status = auditEntryEntity.getStatus().getDisplayName();
        }

        final String errorMessage = auditEntryEntity.getErrorMessage();
        final String errorStackTrace = auditEntryEntity.getErrorStackTrace();

        NotificationConfig notificationConfig = null;
        if (!notifications.isEmpty() && notifications.get(0) != null) {
            notificationConfig = (NotificationConfig) notificationContentConverter.populateRestModelFromDatabaseEntity(notifications.get(0).getNotificationEntity());
            final Set<String> notificationTypes = notifications.stream().map(notification -> notification.getNotificationType().name()).collect(Collectors.toSet());
            notificationConfig.setNotificationTypes(notificationTypes);
            final Set<ComponentConfig> components = notifications.stream().map(notification -> new ComponentConfig(notification.getComponentName(), notification.getComponentVersion(), notification.getPolicyRuleName(),
                    notification.getPolicyRuleUser())).collect(Collectors.toSet());
            notificationConfig.setComponents(components);
        }

        String distributionConfigName = null;
        String eventType = null;
        if (commonConfigEntity.isPresent()) {
            distributionConfigName = commonConfigEntity.get().getName();
            eventType = commonConfigEntity.get().getDistributionType();
        }

        return new AuditEntryRestModel(id, distributionConfigName, eventType, timeCreated, timeLastSent, status, errorMessage, errorStackTrace, notificationConfig);
    }

}
