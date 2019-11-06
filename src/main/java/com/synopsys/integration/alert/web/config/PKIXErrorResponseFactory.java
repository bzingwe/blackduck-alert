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
package com.synopsys.integration.alert.web.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLHandshakeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.ResponseBodyBuilder;
import com.synopsys.integration.alert.common.rest.ResponseFactory;

@Component
public class PKIXErrorResponseFactory {
    private final Logger logger = LoggerFactory.getLogger(PKIXErrorResponseFactory.class);
    private final String BLACKDUCK_GITHUB_DEPLOYMENT_URL = "https://github.com/blackducksoftware/blackduck-alert/blob/master/deployment";
    private final String ALERT_DEPLOYMENT_DOCKER_SWARM = "docker-swarm";
    private final String ALERT_DEPLOYMENT_DOCKER_COMPOSE = "docker-compose";
    private final String ALERT_DEPLOYMENT_KUBERNETES = "kubernetes";

    private Gson gson;
    private ResponseFactory responseFactory;

    @Autowired
    public PKIXErrorResponseFactory(Gson gson, ResponseFactory responseFactory) {
        this.gson = gson;
        this.responseFactory = responseFactory;
    }

    public Optional<ResponseEntity<String>> createSSLExceptionResponse(String id, Exception e) {
        if (isPKIXError(e)) {
            logger.debug("Found an error regarding PKIX, creating a unique response...");
            Map<String, Object> pkixErrorBody = Map.of("header", createHeader(), "title", createTitle(), "info", createInfo());
            String pkixError = gson.toJson(pkixErrorBody);
            ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(id, pkixError);
            responseBodyBuilder.put("isDetailed", true);
            ResponseEntity<String> badRequestResponse = responseFactory.createBadRequestResponse(id, responseBodyBuilder.build());
            return Optional.of(badRequestResponse);
        }

        return Optional.empty();
    }

    private boolean isPKIXError(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause instanceof SSLHandshakeException) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }

    private String createHeader() {
        return "There were issues with your Certificates.";
    }

    private String createTitle() {
        return "To resolve this issue, use the appropriate link below to properly install your certificates and then restart Alert.";
    }

    private List<String> createInfo() {
        String swarm = String.format("Docker Swarm - %s", createLinkToReadme(ALERT_DEPLOYMENT_DOCKER_SWARM));
        String compose = String.format("Docker Compose - %s", createLinkToReadme(ALERT_DEPLOYMENT_DOCKER_COMPOSE));
        String kubes = String.format("Kubernetes - %s", createLinkToReadme(ALERT_DEPLOYMENT_KUBERNETES));

        return List.of(swarm, compose, kubes);
    }

    private String createLinkToReadme(String deploymentType) {
        return String.format("%s/%s/README.md#certificates", BLACKDUCK_GITHUB_DEPLOYMENT_URL, deploymentType);
    }
}
