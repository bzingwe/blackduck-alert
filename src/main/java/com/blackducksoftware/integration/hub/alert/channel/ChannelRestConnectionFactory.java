/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.channel;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

@Component
public class ChannelRestConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRestConnectionFactory.class);

    private final GlobalProperties globalProperties;

    @Autowired
    public ChannelRestConnectionFactory(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    public RestConnection createUnauthenticatedRestConnection(final String stringUrl) throws EncryptionException {
        final URL url = getUrlFromString(stringUrl);
        return createUnauthenticatedRestConnection(url);
    }

    public RestConnection createUnauthenticatedRestConnection(final URL url) throws EncryptionException {
        return createUnauthenticatedRestConnection(url, new Slf4jIntLogger(logger), 5 * 60 * 1000);
    }

    public RestConnection createUnauthenticatedRestConnection(final URL url, final IntLogger intLogger, final int timeout) throws EncryptionException {
        // TODO should credentials be encrypted?
        final Credentials credentials = new Credentials(globalProperties.hubProxyUsername, globalProperties.hubProxyPassword);
        final int proxyPort = Integer.parseInt(globalProperties.hubProxyPort);
        final RestConnection connection = new UnauthenticatedRestConnection(intLogger, url, timeout, new ProxyInfo(globalProperties.hubProxyHost, proxyPort, credentials, null));
        try {
            connection.connect();
            return connection;
        } catch (final IntegrationException e) {
            logger.error("Could not connect to " + url.toString(), e);
            return null;
        }
    }

    private URL getUrlFromString(final String apiUrl) {
        URL url = null;
        try {
            url = new URL(apiUrl);
        } catch (final MalformedURLException e) {
            logger.error("Problem generating the URL: " + apiUrl, e);
        }
        return url;
    }

}
