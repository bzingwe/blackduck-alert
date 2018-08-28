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
package com.synopsys.integration.alert.web.config.actions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class SingleEntityConfigActions extends DescriptorConfigActions {

    @Autowired
    public SingleEntityConfigActions(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public String validateConfig(Config restModel, final RestApi descriptor) throws AlertFieldException {
        final List<? extends DatabaseEntity> globalConfigs = descriptor.readEntities();
        if (globalConfigs.size() == 1) {
            try {
                restModel = updateEntityWithSavedEntity(restModel, globalConfigs.get(0));
            } catch (final AlertException e) {
                return "Error updating config.";
            }
        }
        return super.validateConfig(restModel, descriptor);
    }

    @Override
    public String testConfig(Config restModel, final RestApi descriptor) throws IntegrationException {
        final List<? extends DatabaseEntity> globalConfigs = descriptor.readEntities();
        if (globalConfigs.size() == 1) {
            restModel = updateEntityWithSavedEntity(restModel, globalConfigs.get(0));
            return super.testConfig(restModel, descriptor);
        }
        return "Config did not have the expected number of rows: Expected 1, but found " + globalConfigs.size();
    }

}