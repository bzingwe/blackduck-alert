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
package com.synopsys.integration.alert.web.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.web.model.UserConfig;

@Component
@Transactional
public class UserActions {
    private static final String FIELD_KEY_USER_MGMT_USERNAME = "username";
    private static final String FIELD_KEY_USER_MGMT_PASSWORD = "password";
    private static final String FIELD_KEY_USER_MGMT_EMAILADDRESS = "emailAddress";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;
    private UserAccessor userAccessor;

    public UserActions(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public Collection<UserConfig> getUsers() {
        return userAccessor.getUsers().stream()
                   .map(this::convertToCustomUserRoleModel)
                   .collect(Collectors.toList());
    }

    public Optional<UserConfig> getUser(String userName) throws AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return userAccessor.getUser(userName).map(this::convertToCustomUserRoleModel);
    }

    public UserConfig createUser(String userName, String password, String emailAddress) throws AlertDatabaseConstraintException, AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        validateCreationUserName(fieldErrors, userName);
        validatePasswordLength(fieldErrors, password);
        validateRequiredField(FIELD_KEY_USER_MGMT_EMAILADDRESS, fieldErrors, emailAddress);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        UserModel userModel = userAccessor.addUser(userName, password, emailAddress);
        return convertToCustomUserRoleModel(userModel);
    }

    public void deleteUser(String userName) throws AlertDatabaseConstraintException, AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        //TODO check to make sure this isn't an external user.
        userAccessor.deleteUser(userName);
    }

    public boolean updatePassword(String userName, String newPassword) throws AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        validatePasswordLength(fieldErrors, newPassword);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return userAccessor.changeUserPassword(userName, newPassword);
    }

    public boolean updateEmail(String userName, String emailAddress) throws AlertFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        validateRequiredField(FIELD_KEY_USER_MGMT_EMAILADDRESS, fieldErrors, emailAddress);
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return userAccessor.changeUserEmailAddress(userName, emailAddress);
    }

    private UserConfig convertToCustomUserRoleModel(UserModel userModel) {
        return new UserConfig(
            userModel.getName(),
            userModel.getPassword(),
            userModel.getEmailAddress(),
            userModel.getRoleNames(),
            userModel.isExpired(),
            userModel.isLocked(),
            userModel.isPasswordExpired(),
            userModel.isEnabled());
    }

    private void validateRequiredField(String fieldKey, Map<String, String> fieldErrors, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)) {
            fieldErrors.put(fieldKey, "This field is required.");
        }
    }

    private void validateCreationUserName(Map<String, String> fieldErrors, String userName) {
        validateRequiredField(FIELD_KEY_USER_MGMT_USERNAME, fieldErrors, userName);
        Optional<UserModel> userModel = userAccessor.getUser(userName);
        userModel.ifPresent(user -> fieldErrors.put(FIELD_KEY_USER_MGMT_USERNAME, "A user with that username already exists."));
    }

    private void validatePasswordLength(Map<String, String> fieldErrors, String passwordValue) {
        validateRequiredField(FIELD_KEY_USER_MGMT_PASSWORD, fieldErrors, passwordValue);
        if (fieldErrors.isEmpty()) {
            if (DEFAULT_PASSWORD_LENGTH > passwordValue.length()) {
                fieldErrors.put(FIELD_KEY_USER_MGMT_PASSWORD, "The password need to be at least 8 characters long.");
            }
        }
    }
}
