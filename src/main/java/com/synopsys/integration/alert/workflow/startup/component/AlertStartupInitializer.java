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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.workflow.startup.StartupComponent;
import com.synopsys.integration.alert.web.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.web.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.web.processor.FieldModelProcessor;

@Component
@Order(1)
public class AlertStartupInitializer extends StartupComponent {
    private static final String LINE_DIVIDER = "---------------------------------";
    private final Logger logger = LoggerFactory.getLogger(AlertStartupInitializer.class);
    private final Environment environment;
    private final DescriptorMap descriptorMap;
    private final DescriptorAccessor descriptorAccessor;
    private final ConfigurationAccessor fieldConfigurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final FieldModelProcessor fieldModelProcessor;
    private final SettingsDescriptorKey settingsDescriptorKey;

    @Autowired
    public AlertStartupInitializer(DescriptorMap descriptorMap, Environment environment, DescriptorAccessor descriptorAccessor, ConfigurationAccessor fieldConfigurationAccessor,
        ConfigurationFieldModelConverter modelConverter, FieldModelProcessor fieldModelProcessor, SettingsDescriptorKey settingsDescriptorKey) {
        this.descriptorMap = descriptorMap;
        this.environment = environment;
        this.descriptorAccessor = descriptorAccessor;
        this.fieldConfigurationAccessor = fieldConfigurationAccessor;
        this.modelConverter = modelConverter;
        this.fieldModelProcessor = fieldModelProcessor;
        this.settingsDescriptorKey = settingsDescriptorKey;
    }

    @Override
    protected void initialize() {
        try {
            initializeConfigs();
        } catch (final Exception e) {
            logger.error("Error inserting startup values", e);
        }
    }

    private void initializeConfigs() throws IllegalArgumentException, SecurityException {
        logger.info(String.format("** %s **", LINE_DIVIDER));
        logger.info("Initializing descriptors with environment variables...");
        final boolean overwriteCurrentConfig = manageEnvironmentOverrideEnabled();
        logger.info("Environment variables override configuration: {}", overwriteCurrentConfig);
        initializeConfiguration(List.of(settingsDescriptorKey.getUniversalKey()), overwriteCurrentConfig);
        final List<String> descriptorNames = descriptorMap.getDescriptorMap().keySet().stream().filter(key -> !key.equals(settingsDescriptorKey.getUniversalKey())).sorted().collect(Collectors.toList());
        initializeConfiguration(descriptorNames, overwriteCurrentConfig);
    }

    private boolean manageEnvironmentOverrideEnabled() {
        boolean environmentOverride = false;
        try {
            // determine if the environment variables should overwrite based on the settings configuration.
            Optional<ConfigurationModel> settingsConfiguration = findSettingsConfiguration();
            final String fieldKey = SettingsDescriptor.KEY_STARTUP_ENVIRONMENT_VARIABLE_OVERRIDE;

            final String environmentFieldKey = convertKeyToProperty(settingsDescriptorKey.getUniversalKey(), fieldKey);
            final Optional<String> environmentValue = getEnvironmentValue(environmentFieldKey);

            if (environmentValue.isPresent() && settingsConfiguration.isPresent()) {
                final ConfigurationModel foundModel = settingsConfiguration.get();
                final Map<String, ConfigurationFieldModel> fieldModelMap = foundModel.getCopyOfKeyToFieldMap();
                fieldModelMap.get(fieldKey).setFieldValue(String.valueOf(Boolean.valueOf(environmentValue.get())));
                settingsConfiguration = Optional.ofNullable(fieldConfigurationAccessor.updateConfiguration(foundModel.getConfigurationId(), fieldModelMap.values()));
            }

            environmentOverride = settingsConfiguration
                                      .flatMap(configurationModel -> configurationModel.getField(fieldKey))
                                      .flatMap(ConfigurationFieldModel::getFieldValue)
                                      .map(value -> Boolean.valueOf(value)).orElse(Boolean.FALSE);
        } catch (final AlertDatabaseConstraintException ex) {
            logger.error("Error checking environment override", ex);
        }
        return environmentOverride;
    }

    private Optional<ConfigurationModel> findSettingsConfiguration() throws AlertDatabaseConstraintException {
        final List<ConfigurationModel> settingsConfigurationModels = fieldConfigurationAccessor.getConfigurationByDescriptorNameAndContext(settingsDescriptorKey.getUniversalKey(), ConfigContextEnum.GLOBAL);
        return settingsConfigurationModels.stream().findFirst();
    }

    // TODO consider using a Collection of DescriptorKeys instead
    private void initializeConfiguration(final Collection<String> descriptorNames, final boolean overwriteCurrentConfig) {
        for (final String descriptorName : descriptorNames) {
            logger.info(LINE_DIVIDER);
            logger.info("Descriptor: {}", descriptorName);
            logger.info(LINE_DIVIDER);
            logger.info("  Starting Descriptor Initialization...");
            try {
                final List<DefinedFieldModel> fieldsForDescriptor = descriptorAccessor.getFieldsForDescriptor(descriptorName, ConfigContextEnum.GLOBAL).stream()
                                                                        .sorted(Comparator.comparing(DefinedFieldModel::getKey))
                                                                        .collect(Collectors.toList());
                final List<ConfigurationModel> foundConfigurationModels = fieldConfigurationAccessor.getConfigurationByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);

                Map<String, ConfigurationFieldModel> existingConfiguredFields = new HashMap<>();
                foundConfigurationModels.stream()
                    .forEach(config ->
                                 existingConfiguredFields.putAll(
                                     config.getCopyOfKeyToFieldMap().entrySet().stream()
                                         .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()))
                                 )
                    );

                final Set<ConfigurationFieldModel> configurationModels = createFieldModelsFromDefinedFields(descriptorName, fieldsForDescriptor, existingConfiguredFields);
                logConfiguration(configurationModels);
                updateConfigurationFields(descriptorName, overwriteCurrentConfig, foundConfigurationModels, configurationModels);

            } catch (final IllegalArgumentException | SecurityException | AlertException ex) {
                logger.error("error initializing descriptor", ex);
            } finally {
                logger.info("  Finished Descriptor Initialization...");
                logger.info(LINE_DIVIDER);
            }
        }
    }

    private Set<ConfigurationFieldModel> createFieldModelsFromDefinedFields(final String descriptorName, final List<DefinedFieldModel> fieldsForDescriptor, Map<String, ConfigurationFieldModel> existingConfiguredFields) {
        final Set<ConfigurationFieldModel> configurationModels = new HashSet<>();
        logger.info("  ### Environment Variables ### ");
        for (final DefinedFieldModel fieldModel : fieldsForDescriptor) {
            final String key = fieldModel.getKey();
            final String convertedKey = convertKeyToProperty(descriptorName, key);
            final boolean hasEnvironmentValue = hasEnvironmentValue(convertedKey);
            logger.info("    {}", convertedKey);
            logger.debug("         Environment Variable Found - {}", hasEnvironmentValue);
            String defaultValue = null;
            if (existingConfiguredFields.containsKey(key)) {
                defaultValue = existingConfiguredFields.get(key).getFieldValue().get();
            }

            getEnvironmentValue(convertedKey, defaultValue)
                .flatMap(value -> modelConverter.convertFromDefinedFieldModel(fieldModel, value, StringUtils.isNotBlank(value)))
                .ifPresent(configurationModels::add);
        }
        return configurationModels;
    }

    private void updateConfigurationFields(final String descriptorName, final boolean overwriteCurrentConfig, final List<ConfigurationModel> foundConfigurationModels, final Set<ConfigurationFieldModel> configurationModels)
        throws AlertException {
        Optional<ConfigurationModel> optionalFoundModel = foundConfigurationModels
                                                              .stream()
                                                              .findFirst()
                                                              .filter(model -> overwriteCurrentConfig);
        logger.info("  ### Processing Configuration ###");
        if (optionalFoundModel.isPresent()) {
            final ConfigurationModel foundModel = optionalFoundModel.get();
            logger.info("    Overwriting values with environment.");
            final Collection<ConfigurationFieldModel> updatedFields = updateAction(descriptorName, configurationModels);
            fieldConfigurationAccessor.updateConfiguration(foundModel.getConfigurationId(), updatedFields);
        } else if (foundConfigurationModels.isEmpty()) {
            logger.info("    Writing initial values from environment.");
            final Collection<ConfigurationFieldModel> savedFields = saveAction(descriptorName, configurationModels);
            fieldConfigurationAccessor.createConfiguration(descriptorName, ConfigContextEnum.GLOBAL, savedFields);
        }
    }

    private Collection<ConfigurationFieldModel> updateAction(final String descriptorName, final Collection<ConfigurationFieldModel> configurationFieldModels)
        throws AlertException {
        final Map<String, FieldValueModel> fieldValueModelMap = modelConverter.convertToFieldValuesMap(configurationFieldModels);
        final FieldModel fieldModel = new FieldModel(descriptorName, ConfigContextEnum.GLOBAL.name(), fieldValueModelMap);
        final FieldModel updatedFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
        return modelConverter.convertToConfigurationFieldModelMap(updatedFieldModel).values();
    }

    private Collection<ConfigurationFieldModel> saveAction(final String descriptorName, final Collection<ConfigurationFieldModel> configurationFieldModels) throws AlertException {
        final Map<String, FieldValueModel> fieldValueModelMap = modelConverter.convertToFieldValuesMap(configurationFieldModels);
        final FieldModel fieldModel = new FieldModel(descriptorName, ConfigContextEnum.GLOBAL.name(), fieldValueModelMap);
        final FieldModel savedFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
        return modelConverter.convertToConfigurationFieldModelMap(savedFieldModel).values();
    }

    private String convertKeyToProperty(final String descriptorName, final String key) {
        final String keyUnderscores = key.replace(".", "_");
        return String.join("_", "alert", descriptorName, keyUnderscores).toUpperCase();
    }

    private boolean hasEnvironmentValue(final String propertyKey) {
        final String value = System.getProperty(propertyKey);
        return StringUtils.isNotBlank(value) || environment.containsProperty(propertyKey);
    }

    private Optional<String> getEnvironmentValue(final String propertyKey) {
        return getEnvironmentValue(propertyKey, null);
    }

    private Optional<String> getEnvironmentValue(final String propertyKey, String defaultValue) {
        String value = System.getProperty(propertyKey);
        if (StringUtils.isBlank(value)) {
            value = environment.getProperty(propertyKey);
        }
        boolean propertyIsSet = System.getProperties().contains(propertyKey) || environment.containsProperty(propertyKey);
        if (!propertyIsSet && null != defaultValue && StringUtils.isBlank(value)) {
            value = defaultValue;
        }
        return Optional.ofNullable(value);
    }

    private void logConfiguration(Collection<ConfigurationFieldModel> fieldModels) {
        if (!fieldModels.isEmpty()) {
            logger.info("  ");
            logger.info("  ### Configuration ### ");
            fieldModels.forEach(this::logField);
            logger.info("  ");
        }
    }

    private void logField(ConfigurationFieldModel fieldModel) {
        String value = fieldModel.isSensitive() ? "**********" : String.valueOf(fieldModel.getFieldValues());
        logger.info("    {} = {}", fieldModel.getFieldKey(), value);
    }

}
