/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointButtonField extends EndpointField {
    private boolean successBox;
    private List<ConfigField> subFields;

    public EndpointButtonField(String key, String label, String description, String buttonLabel) {
        super(key, label, description, FieldType.ENDPOINT_BUTTON, buttonLabel, CustomEndpointManager.CUSTOM_ENDPOINT_URL);
        this.successBox = Boolean.FALSE;
        this.subFields = new LinkedList<>();
    }

    public EndpointButtonField applySuccessBox(boolean successBox) {
        this.successBox = successBox;
        return this;
    }

    public EndpointButtonField applySubFields(List<ConfigField> subFields) {
        if (null != subFields) {
            this.subFields = subFields;
        }
        return this;
    }

    public EndpointButtonField applySubField(ConfigField field) {
        if (null != field) {
            subFields.add(field);
        }
        return this;
    }

    public Boolean getSuccessBox() {
        return successBox;
    }

    public List<ConfigField> getSubFields() {
        return subFields;
    }

}
