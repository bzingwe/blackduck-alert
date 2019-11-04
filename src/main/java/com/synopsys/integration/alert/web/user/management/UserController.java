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

import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.web.controller.BaseController;

@RestController
@RequestMapping(UserController.USER_BASE_PATH)
public class UserController extends BaseController {
    public static final String USER_BASE_PATH = BaseController.BASE_PATH + "/users";
    private final ContentConverter contentConverter;
    private final ResponseFactory responseFactory;
    private final AuthorizationManager authorizationManager;
    private final UserActions userActions;
    private final UserManagementDescriptorKey descriptorKey;

    @Autowired
    public UserController(ContentConverter contentConverter, ResponseFactory responseFactory, AuthorizationManager authorizationManager, UserActions userActions,
        UserManagementDescriptorKey descriptorKey) {
        this.contentConverter = contentConverter;
        this.responseFactory = responseFactory;
        this.authorizationManager = authorizationManager;
        this.userActions = userActions;
        this.descriptorKey = descriptorKey;
    }

    @GetMapping
    public ResponseEntity<String> getAllUsers() {
        if (!hasPermission(authorizationManager::hasReadPermission)) {
            return responseFactory.createForbiddenResponse();
        }
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(userActions.getUsers()));
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserModel userModel) {
        if (!hasPermission(authorizationManager::hasCreatePermission)) {
            return responseFactory.createForbiddenResponse();
        }
        userActions.createUser(userModel.getName(), userModel.getPassword(), userModel.getEmailAddress());
        return responseFactory.createCreatedResponse(ResponseFactory.EMPTY_ID, "User Created.");
    }

    //    @PostMapping(value="/{userName}/roles")
    //    public ResponseEntity<String> assignUserRoles(@RequestBody List<String> roleNames) {
    //        return userManagementActions.
    //    }

    @DeleteMapping(value = "/{userName}")
    public ResponseEntity<String> deleteUser(@PathVariable String userName) {
        if (!hasPermission(authorizationManager::hasDeletePermission)) {
            return responseFactory.createForbiddenResponse();
        }
        userActions.deleteUser(userName);
        return responseFactory.createOkResponse(ResponseFactory.EMPTY_ID, "Deleted");
    }

    private boolean hasPermission(BiFunction<String, String, Boolean> permissionChecker) {
        return permissionChecker.apply(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    }
}
