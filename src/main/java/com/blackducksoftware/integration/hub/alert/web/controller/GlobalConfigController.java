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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.GlobalConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;

@RestController
public class GlobalConfigController implements ConfigController<GlobalConfigEntity, GlobalConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(GlobalConfigController.class);
    private final GlobalConfigActions configActions;
    private final CommonConfigController<GlobalConfigEntity, GlobalConfigRestModel> commonConfigController;

    @Autowired
    GlobalConfigController(final GlobalConfigActions configActions) {
        this.configActions = configActions;
        commonConfigController = new CommonConfigController<>(GlobalConfigEntity.class, GlobalConfigRestModel.class, configActions);
    }

    @Override
    @GetMapping(value = "/configuration/global")
    public List<GlobalConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PostMapping(value = "/configuration/global")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "globalConfig", required = false) @RequestBody final GlobalConfigRestModel globalConfig) {
        return commonConfigController.postConfig(globalConfig);
    }

    @Override
    @PutMapping(value = "/configuration/global")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "globalConfig", required = false) @RequestBody final GlobalConfigRestModel globalConfig) {
        return commonConfigController.putConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final GlobalConfigRestModel globalConfig) {
        // TODO
        return null;
    }

    @Override
    @DeleteMapping(value = "/configuration/global")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "globalConfig", required = false) @RequestBody final GlobalConfigRestModel globalConfig) {
        return commonConfigController.deleteConfig(globalConfig);
    }

    @Override
    @PostMapping(value = "/configuration/global/test")
    public ResponseEntity<String> testConfig(@RequestAttribute(value = "globalConfig", required = false) final GlobalConfigRestModel globalConfig) {
        // TODO implement method for testing the configuration
        return ResponseEntity.notFound().build();
    }

}
