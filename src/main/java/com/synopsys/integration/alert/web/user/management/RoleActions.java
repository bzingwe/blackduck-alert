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
package com.synopsys.integration.alert.web.user.management;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.web.model.RolePermissionsModel;
import com.synopsys.integration.exception.IntegrationException;

@Component
@Transactional
public class RoleActions {
    private static final Logger logger = LoggerFactory.getLogger(RoleActions.class);
    private AuthorizationUtility authorizationUtility;

    @Autowired
    public RoleActions(AuthorizationUtility authorizationUtility) {
        this.authorizationUtility = authorizationUtility;
    }

    public Collection<UserRoleModel> getRoles() {
        return authorizationUtility.getRoles();
    }

    public Optional<UserRoleModel> getRole(String roleName) {
        return authorizationUtility.getRoles().stream()
                   .filter(role -> role.getName().equals(roleName))
                   .findFirst();
    }

    public UserRoleModel createRole(String roleName) throws IntegrationException {
        return authorizationUtility.createRole(roleName);
    }

    public void assignPermissions(List<RolePermissionsModel> permissionModels) {

        Map<String, Map<PermissionKey, EnumSet<AccessOperation>>> rolePermissionsMap = new HashMap<>();
        for (RolePermissionsModel permissionModel : permissionModels) {
            Map<PermissionKey, EnumSet<AccessOperation>> permissionsForRole = rolePermissionsMap.computeIfAbsent(permissionModel.getRoleName(), ignored -> new HashMap<>());
            PermissionKey permissionKey = new PermissionKey(permissionModel.getContext(), permissionModel.getDescriptorKey());
            EnumSet<AccessOperation> operationSet = permissionsForRole.computeIfAbsent(permissionKey, ignored -> EnumSet.noneOf(AccessOperation.class));

            for (String operationName : permissionModel.getOperations()) {
                AccessOperation parsedOperation = AccessOperation.valueOf(operationName);
                operationSet.add(parsedOperation);
            }
        }

        for (Map.Entry<String, Map<PermissionKey, EnumSet<AccessOperation>>> entry : rolePermissionsMap.entrySet()) {
            try {
                authorizationUtility.updatePermissionsForRole(entry.getKey(), new PermissionMatrixModel(entry.getValue()));
            } catch (AlertException ex) {
                logger.error("Failed to assign permissions.", ex);
            }
        }
    }

    public void deleteRole(String roleName) throws AlertDatabaseConstraintException {
        Optional<String> userRole = authorizationUtility.getRoles().stream()
                                        .filter(role -> role.getName().equals(roleName))
                                        .filter(UserRoleModel::isCustom)
                                        .map(UserRoleModel::getName)
                                        .findFirst();
        if (userRole.isPresent()) {
            authorizationUtility.deleteRole(userRole.get());
        }
    }
}
