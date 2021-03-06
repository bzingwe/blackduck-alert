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
package com.synopsys.integration.alert.common.rest.model;

import java.util.Set;

public class JobFieldModel extends AlertSerializableModel {
    private String jobId;
    private Set<FieldModel> fieldModels;

    public JobFieldModel() {
        this(null, null);
    }

    public JobFieldModel(final String jobId, final Set<FieldModel> fieldModels) {
        this.jobId = jobId;
        this.fieldModels = fieldModels;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public Set<FieldModel> getFieldModels() {
        return fieldModels;
    }

    public void setFieldModels(final Set<FieldModel> fieldModels) {
        this.fieldModels = fieldModels;
    }
}
