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
package com.synopsys.integration.alert.workflow.scheduled.frequency;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.util.ChannelEventManager;
import com.synopsys.integration.alert.common.workflow.task.StartupScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.workflow.processor.NotificationProcessor;
import com.synopsys.integration.rest.RestConstants;

public abstract class ProcessingTask extends StartupScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationManager notificationManager;
    private final NotificationProcessor notificationProcessor;
    private final ChannelEventManager eventManager;
    private ZonedDateTime lastRunTime;

    public ProcessingTask(final TaskScheduler taskScheduler, final String taskName, final NotificationManager notificationManager, final NotificationProcessor notificationProcessor,
        final ChannelEventManager eventManager, final TaskManager taskManager) {
        super(taskScheduler, taskName, taskManager);
        this.notificationManager = notificationManager;
        this.notificationProcessor = notificationProcessor;
        this.eventManager = eventManager;
        lastRunTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
    }

    public abstract FrequencyType getDigestType();

    public ZonedDateTime getLastRunTime() {
        return lastRunTime;
    }

    public DateRange getDateRange() {
        final ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final Date startDate = Date.from(lastRunTime.toInstant());
        final Date endDate = Date.from(currentTime.toInstant());
        return DateRange.of(startDate, endDate);
    }

    @Override
    public void runTask() {
        final DateRange dateRange = getDateRange();
        final List<AlertNotificationWrapper> notificationList = read(dateRange);
        final List<DistributionEvent> distributionEvents = notificationProcessor.processNotifications(getDigestType(), notificationList);
        eventManager.sendEvents(distributionEvents);
        lastRunTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
    }

    public List<AlertNotificationWrapper> read(final DateRange dateRange) {
        try {
            final String taskName = getTaskName();
            final Date startDate = dateRange.getStart();
            final Date endDate = dateRange.getEnd();
            logger.info("{} Reading Notifications Between {} and {} ", taskName, RestConstants.formatDate(startDate), RestConstants.formatDate(endDate));
            final List<AlertNotificationWrapper> entityList = notificationManager.findByCreatedAtBetween(startDate, endDate);
            if (entityList.isEmpty()) {
                logger.info("{} Notifications Found: 0", taskName);
                return List.of();
            } else {
                logger.info("{} Notifications Found: {}", taskName, entityList.size());
                return entityList;
            }
        } catch (final Exception ex) {
            logger.error("Error reading Digest Notification Data", ex);
        }
        return List.of();
    }
}
