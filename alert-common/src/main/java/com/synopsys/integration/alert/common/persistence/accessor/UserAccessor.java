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
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.UserModel;

public interface UserAccessor {

    List<UserModel> getUsers();

    Optional<UserModel> getUser(String username);

    UserModel addOrUpdateUser(final UserModel user);

    UserModel addOrUpdateUser(UserModel user, boolean passwordEncoded);
    
    UserModel addUser(String userName, String password, String emailAddress);

    boolean assignRoles(String username, Set<Long> roleIds);

    boolean changeUserPassword(String username, String newPassword);

    boolean changeUserEmailAddress(String username, String emailAddress);

    void deleteUser(String userName);
}
