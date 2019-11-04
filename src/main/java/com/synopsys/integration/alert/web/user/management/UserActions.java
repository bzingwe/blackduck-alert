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
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

@Component
@Transactional
public class UserActions {
    private AuthorizationUtility authorizationUtility;
    private UserAccessor userAccessor;

    public UserActions(AuthorizationUtility authorizationUtility, UserAccessor userAccessor) {
        this.authorizationUtility = authorizationUtility;
        this.userAccessor = userAccessor;
    }

    public Collection<UserModel> getUsers() {
        return userAccessor.getUsers().stream()
                   .map(user -> UserModel.of(user.getName(), "", user.getEmailAddress(), user.getRoles()))
                   .collect(Collectors.toList());
    }

    public UserModel createUser(String userName, String password, String emailAddress) {
        return userAccessor.addUser(userName, password, emailAddress);
    }

    public void deleteUser(String userName) {
        userAccessor.deleteUser(userName);
    }

    public boolean updatePassword(String userName, String newPassword) {
        return userAccessor.changeUserPassword(userName, newPassword);
    }

    public boolean updateEmail(String userName, String emailAddress) {
        return userAccessor.changeUserEmailAddress(userName, emailAddress);
    }
}
