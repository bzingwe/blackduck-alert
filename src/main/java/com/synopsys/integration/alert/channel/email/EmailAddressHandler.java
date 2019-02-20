/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.channel.email;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.data.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.provider.Provider;

@Component
public class EmailAddressHandler {
    private final DescriptorMap descriptorMap;

    @Autowired
    public EmailAddressHandler(@Lazy final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    public FieldAccessor updateEmailAddresses(final String provider, final AggregateMessageContent content, final FieldAccessor originalAccessor) {
        if (StringUtils.isBlank(provider)) {
            return originalAccessor;
        }
        final Optional<ProviderDescriptor> descriptor = descriptorMap.getProviderDescriptor(provider);
        return descriptor
                   .map(ProviderDescriptor::getProvider)
                   .map(Provider::getEmailHandler)
                   .map(emailHandler -> emailHandler.updateFieldAccessor(content, originalAccessor))
                   .orElse(originalAccessor);
    }
}
