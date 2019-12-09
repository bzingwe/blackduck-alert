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
package com.synopsys.integration.alert.web.model;

import java.util.Set;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class RolePermissionModel extends AlertSerializableModel {
    private String roleName;
    private Set<PermissionModel> permissions;

    public RolePermissionModel() {
    }

    public RolePermissionModel(String roleName, Set<PermissionModel> permissions) {
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public Set<PermissionModel> getPermissions() {
        return permissions;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setPermissions(Set<PermissionModel> permissionModels) {
        this.permissions = permissionModels;
    }
}
