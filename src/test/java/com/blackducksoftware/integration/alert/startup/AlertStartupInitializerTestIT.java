package com.blackducksoftware.integration.alert.startup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.channel.PropertyInitializer;
import com.blackducksoftware.integration.alert.channel.email.EmailDescriptor;
import com.blackducksoftware.integration.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailRepository;
import com.blackducksoftware.integration.alert.descriptor.Descriptor;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.startup.AlertStartupInitializer;
import com.blackducksoftware.integration.alert.startup.AlertStartupProperty;

public class AlertStartupInitializerTestIT {

    @Test
    public void testInitializeConfigs() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
        assertFalse(initializer.getAlertProperties().isEmpty());
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testInitializeConfigsEmptyInitializerList() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList();
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
        assertTrue(initializer.getAlertProperties().isEmpty());
        assertTrue(initializer.getAlertPropertyNameSet().isEmpty());
    }

    @Test
    public void testSetRestModelValue() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
        final String value = "newValue";

        final GlobalEmailConfigRestModel globalRestModel = new GlobalEmailConfigRestModel();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        initializer.setRestModelValue(value, globalRestModel, property);

        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        final Object fieldValue = declaredField.get(globalRestModel);
        declaredField.setAccessible(accessible);
        assertEquals(value, fieldValue);
    }

    @Test
    public void testSetRestModelValueEmtpyValue() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();

        final GlobalEmailConfigRestModel globalRestModel = new GlobalEmailConfigRestModel();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        initializer.setRestModelValue(null, globalRestModel, property);

        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        final Object fieldValue = declaredField.get(globalRestModel);
        declaredField.setAccessible(accessible);
        assertNull(fieldValue);
    }

    @Test
    public void testCanConvertFalse() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenReturn(false);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
        final GlobalEmailConfigRestModel globalRestModel = new GlobalEmailConfigRestModel();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        initializer.setRestModelValue("a value that can't be converted", globalRestModel, property);

        final Field declaredField = globalRestModel.getClass().getDeclaredField(property.getFieldName());
        final boolean accessible = declaredField.isAccessible();

        declaredField.setAccessible(true);
        final Object fieldValue = declaredField.get(globalRestModel);
        declaredField.setAccessible(accessible);
        assertNull(fieldValue);
    }

    @Test
    public void testSystemPropertyExists() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenReturn(true);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        final MockEmailGlobalEntity mockEmailGlobalEntity = new MockEmailGlobalEntity();
        Mockito.when(globalEmailRepository.save(Mockito.any())).thenReturn(mockEmailGlobalEntity.createGlobalEntity());
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);

        initializer.initializeConfigs();
        final AlertStartupProperty property = initializer.getAlertProperties().get(0);
        final String value = "a system property value";
        System.setProperty(property.getPropertyKey(), value);
        initializer.initializeConfigs();
    }

    @Test
    public void testSetRestModelSecurityException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenThrow(new SecurityException());
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
    }

    @Test
    public void testSetRestModelIllegalArgumentException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = Mockito.mock(ConversionService.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        Mockito.when(conversionService.canConvert(Mockito.any(Class.class), Mockito.any(Class.class))).thenThrow(new IllegalArgumentException());
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
    }

    @Test
    public void testInitializeConfigsThrowsIllegalArgumentException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        Mockito.doThrow(new IllegalArgumentException()).when(environment).getProperty(Mockito.anyString());
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        throwExceptionTest(environment, conversionService, objectTransformer, globalEmailRepository);
    }

    @Test
    public void testInitializeConfigsThrowsSecurityException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        Mockito.doThrow(new SecurityException()).when(environment).getProperty(Mockito.anyString());
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        throwExceptionTest(environment, conversionService, objectTransformer, globalEmailRepository);
    }

    @Test
    public void testInitializeConfigsThrowsAlertException() throws Exception {
        final Environment environment = Mockito.mock(Environment.class);
        final ConversionService conversionService = new DefaultConversionService();
        final ObjectTransformer objectTransformer = Mockito.mock(ObjectTransformer.class);
        Mockito.doThrow(new AlertException()).when(objectTransformer).configRestModelToDatabaseEntity(Mockito.any(), Mockito.any());
        final GlobalEmailRepository globalEmailRepository = Mockito.mock(GlobalEmailRepository.class);
        throwExceptionTest(environment, conversionService, objectTransformer, globalEmailRepository);
    }

    private void throwExceptionTest(final Environment environment, final ConversionService conversionService, final ObjectTransformer objectTransformer, final GlobalEmailRepository globalEmailRepository) throws Exception {
        final PropertyInitializer propertyInitializer = new PropertyInitializer();
        final List<Descriptor> descriptors = Arrays.asList(new EmailDescriptor(null, globalEmailRepository, null, null, objectTransformer));
        final AlertStartupInitializer initializer = new AlertStartupInitializer(propertyInitializer, descriptors, environment, conversionService);
        initializer.initializeConfigs();
        assertFalse(initializer.getAlertPropertyNameSet().isEmpty());
    }
}