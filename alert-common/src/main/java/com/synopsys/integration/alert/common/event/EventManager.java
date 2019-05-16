/**
 * alert-common
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
package com.synopsys.integration.alert.common.event;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;

@Component
public class EventManager {
    private final JmsTemplate jmsTemplate;
    private final AuditUtility auditUtility;
    private final ContentConverter contentConverter;

    @Autowired
    public EventManager(final ContentConverter contentConverter, @Lazy final AuditUtility auditUtility, final JmsTemplate jmsTemplate) {
        this.contentConverter = contentConverter;
        this.auditUtility = auditUtility;
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public void sendEvents(final List<? extends AlertEvent> eventList) {
        if (!eventList.isEmpty()) {
            eventList.forEach(this::sendEvent);
        }
    }

    @Transactional
    public boolean sendEvent(final AlertEvent event) {
        final String destination = event.getDestination();
        if (event instanceof DistributionEvent) {
            final DistributionEvent distributionEvent = (DistributionEvent) event;
            final UUID jobId = UUID.fromString(distributionEvent.getConfigId());
            final Map<Long, Long> notificationIdToAuditId = auditUtility.createAuditEntry(distributionEvent.getNotificationIdToAuditId(), jobId, distributionEvent.getContent());
            distributionEvent.setNotificationIdToAuditId(notificationIdToAuditId);
            final String jsonMessage = contentConverter.getJsonString(distributionEvent);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        } else {
            final String jsonMessage = contentConverter.getJsonString(event);
            jmsTemplate.convertAndSend(destination, jsonMessage);
        }
        return true;
    }
}