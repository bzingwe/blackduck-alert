/**
 * alert-common
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
package com.synopsys.integration.alert.common.rest;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

public class ResponseBodyBuilder {
    private final JsonObject map;

    public ResponseBodyBuilder() {
        map = new JsonObject();
    }

    public ResponseBodyBuilder(String id, String message) {
        this();
        if (!ResponseFactory.EMPTY_ID.equals(id)) {
            map.addProperty("id", id);
        }
        map.addProperty("message", message);
    }

    public ResponseBodyBuilder(String message) {
        this(ResponseFactory.EMPTY_ID, message);
    }

    public ResponseBodyBuilder put(String key, Boolean value) {
        map.addProperty(key, value);
        return this;
    }

    public ResponseBodyBuilder put(String key, Number value) {
        map.addProperty(key, value);
        return this;
    }

    public ResponseBodyBuilder put(String key, String value) {
        map.addProperty(key, value);
        return this;
    }

    public ResponseBodyBuilder putErrors(Map<String, String> errors) {
        JsonObject element = new JsonObject();
        for (Entry<String, String> entry : errors.entrySet()) {
            element.addProperty(entry.getKey(), entry.getValue());
        }
        map.add("errors", element);
        return this;
    }

    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
