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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.util.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationManager;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.web.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.web.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.workflow.processor.NotificationProcessor;

@Component
public class DailyTask extends ProcessingTask {
    private final Logger logger = LoggerFactory.getLogger(DailyTask.class);
    public static final String TASK_NAME = "daily-frequency";
    public static final String CRON_FORMAT = "0 0 %s 1/1 * ?";
    public static final int DEFAULT_HOUR_OF_DAY = 0;

    private final SchedulingDescriptorKey schedulingDescriptorKey;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public DailyTask(SchedulingDescriptorKey schedulingDescriptorKey, TaskScheduler taskScheduler, NotificationManager notificationManager,
        NotificationProcessor notificationProcessor, ChannelEventManager eventManager, TaskManager taskManager,
        ConfigurationAccessor configurationAccessor) {
        super(taskScheduler, TASK_NAME, notificationManager, notificationProcessor, eventManager, taskManager);
        this.schedulingDescriptorKey = schedulingDescriptorKey;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public FrequencyType getDigestType() {
        return FrequencyType.DAILY;
    }

    @Override
    public String scheduleCronExpression() {
        try {
            final List<ConfigurationModel> schedulingConfigs = configurationAccessor.getConfigurationsByDescriptorName(schedulingDescriptorKey.getUniversalKey());
            final String dailySavedCronValue = schedulingConfigs.stream()
                                                   .findFirst()
                                                   .flatMap(configurationModel -> configurationModel.getField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY))
                                                   .flatMap(ConfigurationFieldModel::getFieldValue)
                                                   .orElse(String.valueOf(DEFAULT_HOUR_OF_DAY));
            return String.format(CRON_FORMAT, dailySavedCronValue);
        } catch (final AlertDatabaseConstraintException e) {
            logger.error("Error connecting to DB", e);
        }

        return String.format(CRON_FORMAT, DEFAULT_HOUR_OF_DAY);
    }

}
