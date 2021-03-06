/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.jira.server.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointButtonField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class JiraServerGlobalUIConfig extends UIConfig {
    public static final String LABEL_SERVER_ADMIN_USER_NAME = "Admin User Name";
    public static final String LABEL_SERVER_ADMIN_PASSWORD = "Admin Password";
    public static final String LABEL_SERVER_CONFIGURE_PLUGIN = "Configure Jira server plugin";
    public static final String DESCRIPTION_SERVER_ADMIN_USER_NAME = "The user name of the admin user to log into the Jira server.";
    public static final String DESCRIPTION_SERVER_ADMIN_PASSWORD = "The admin user's password  used to authenticate to the Jira server.";
    public static final String DESCRIPTION_SERVER_CONFIGURE_PLUGIN = "Installs a required plugin on the Jira server.";
    public static final String BUTTON_LABEL_PLUGIN_CONFIGURATION = "Install Plugin Remotely";
    private static final String LABEL_SERVER_URL = "Url";
    private static final String DESCRIPTION_SERVER_URL = "The URL of the Jira server";
    private EncryptionValidator encryptionValidator;

    @Autowired
    public JiraServerGlobalUIConfig(EncryptionValidator encryptionValidator) {
        super(JiraServerDescriptor.JIRA_LABEL, JiraServerDescriptor.JIRA_DESCRIPTION, JiraServerDescriptor.JIRA_URL);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField serverUrlField = new URLInputConfigField(JiraServerDescriptor.KEY_SERVER_URL, LABEL_SERVER_URL, DESCRIPTION_SERVER_URL).applyRequired(true);
        ConfigField jiraUserName = new TextInputConfigField(JiraServerDescriptor.KEY_SERVER_USERNAME, LABEL_SERVER_ADMIN_USER_NAME, DESCRIPTION_SERVER_ADMIN_USER_NAME).applyRequired(true);
        ConfigField jiraPassword = new PasswordConfigField(JiraServerDescriptor.KEY_SERVER_PASSWORD, LABEL_SERVER_ADMIN_PASSWORD, DESCRIPTION_SERVER_ADMIN_PASSWORD, encryptionValidator).applyRequired(true);

        ConfigField jiraConfigurePlugin = new EndpointButtonField(JiraServerDescriptor.KEY_JIRA_SERVER_CONFIGURE_PLUGIN, LABEL_SERVER_CONFIGURE_PLUGIN, DESCRIPTION_SERVER_CONFIGURE_PLUGIN, BUTTON_LABEL_PLUGIN_CONFIGURATION)
                                              .applyRequestedDataFieldKey(JiraServerDescriptor.KEY_SERVER_URL)
                                              .applyRequestedDataFieldKey(JiraServerDescriptor.KEY_SERVER_USERNAME)
                                              .applyRequestedDataFieldKey(JiraServerDescriptor.KEY_SERVER_PASSWORD);

        return List.of(serverUrlField, jiraUserName, jiraPassword, jiraConfigurePlugin);
    }

}
