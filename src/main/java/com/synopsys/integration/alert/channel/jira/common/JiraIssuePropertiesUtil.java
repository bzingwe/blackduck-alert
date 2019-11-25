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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.issuetracker.jira.common.JiraIssueSearchProperties;

public class JiraIssuePropertiesUtil {
    public static final IssueSearchProperties create(String providerName, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem componentItem, String trackingKey) {
        Optional<LinkableItem> subComponent = componentItem != null ? componentItem.getSubComponent() : Optional.empty();
        String category = componentItem != null ? componentItem.getCategory() : null;
        String subTopicName = nullableSubTopic != null ? nullableSubTopic.getName() : null;
        String subTopicValue = nullableSubTopic != null ? nullableSubTopic.getValue() : null;
        String componentName = componentItem != null ? componentItem.getComponent().getName() : null;
        String componentValue = componentItem != null ? componentItem.getComponent().getValue() : null;

        return new JiraIssueSearchProperties(providerName, topic.getName(), topic.getValue(), subTopicName, subTopicValue,
            category, componentName, componentValue, subComponent.map(LinkableItem::getName).orElse(null), subComponent.map(LinkableItem::getValue).orElse(null), trackingKey);
    }
}
