package com.blackducksoftware.integration.hub.alert.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.AlertEnvironment;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.scheduled.task.PhoneHomeTask;
import com.blackducksoftware.integration.hub.alert.scheduling.mock.MockGlobalSchedulingEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingRepository;

public class StartupManagerTest {
    private OutputLogger outputLogger;
    private AlertEnvironment alertEnvironment;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
        alertEnvironment = new AlertEnvironment();
    }

    @After
    public void cleanup() throws IOException {
        if (outputLogger != null) {
            outputLogger.cleanup();
        }
    }

    @Test
    public void testLogConfiguration() throws IOException {
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties();
        final TestGlobalProperties mockTestGlobalProperties = Mockito.spy(testGlobalProperties);
        Mockito.when(mockTestGlobalProperties.getHubProxyPassword()).thenReturn("not_blank_data");
        final StartupManager startupManager = new StartupManager(null, mockTestGlobalProperties, null, null, null, null, null);

        startupManager.logConfiguration();
        assertTrue(outputLogger.isLineContainingText("Hub Proxy Authenticated: true"));
    }

    @Test
    public void testInitializeCronJobs() throws IOException {
        final PhoneHomeTask phoneHomeTask = Mockito.mock(PhoneHomeTask.class);
        Mockito.doNothing().when(phoneHomeTask).scheduleExecution(Mockito.anyString());
        final AccumulatorConfig accumulatorConfig = Mockito.mock(AccumulatorConfig.class);
        Mockito.doNothing().when(accumulatorConfig).scheduleExecution(Mockito.anyString());
        Mockito.doReturn(1L).when(accumulatorConfig).getMillisecondsToNextRun();
        final DailyDigestBatchConfig dailyDigestBatchConfig = Mockito.mock(DailyDigestBatchConfig.class);
        Mockito.doNothing().when(dailyDigestBatchConfig).scheduleExecution(Mockito.anyString());
        Mockito.doReturn("time").when(dailyDigestBatchConfig).getFormatedNextRunTime();
        final PurgeConfig purgeConfig = Mockito.mock(PurgeConfig.class);
        Mockito.doNothing().when(purgeConfig).scheduleExecution(Mockito.anyString());
        Mockito.doReturn("time").when(purgeConfig).getFormatedNextRunTime();
        final GlobalSchedulingRepository globalSchedulingRepository = Mockito.mock(GlobalSchedulingRepository.class);
        final MockGlobalSchedulingEntity mockGlobalSchedulingEntity = new MockGlobalSchedulingEntity();
        final GlobalSchedulingConfigEntity entity = mockGlobalSchedulingEntity.createGlobalEntity();
        Mockito.when(globalSchedulingRepository.save(Mockito.any(GlobalSchedulingConfigEntity.class))).thenReturn(entity);
        final StartupManager startupManager = new StartupManager(globalSchedulingRepository, null, accumulatorConfig, dailyDigestBatchConfig, purgeConfig, phoneHomeTask, null);

        startupManager.initializeCronJobs();

        final String expectedLog = entity.toString();
        assertTrue(outputLogger.isLineContainingText(expectedLog));
    }

    @Test
    public void testValidateProviders() throws IOException {
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties();
        final StartupManager startupManager = new StartupManager(null, testGlobalProperties, null, null, null, null, null);

        startupManager.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testValidateHubProviderNullURL() throws IOException {
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties();
        final StartupManager startupManager = new StartupManager(null, testGlobalProperties, null, null, null, null, null);
        testGlobalProperties.setHubUrl(null);
        startupManager.validateHubProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Hub Provider..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Invalid; cause: Hub URL missing..."));
    }

    @Test
    public void testValidateHubProviderLocalhostURL() throws IOException {
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties();
        final StartupManager startupManager = new StartupManager(null, testGlobalProperties, null, null, null, null, null);
        testGlobalProperties.setHubUrl("https://localhost:443");
        startupManager.validateHubProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Hub Provider..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Using localhost because PUBLIC_HUB_WEBSERVER_HOST environment variable is not set"));
    }

    @Test
    public void testValidateHubProviderHubWebserverEnvironmentSet() throws IOException {
        final AlertEnvironment alertEnvironment = Mockito.mock(AlertEnvironment.class);
        Mockito.when(alertEnvironment.getVariable(AlertEnvironment.ENV_PUBLIC_HUB_WEBSERVER_HOST)).thenReturn("localhost");
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties(alertEnvironment, Mockito.mock(GlobalHubRepository.class));
        testGlobalProperties.setHubUrl("https://localhost:443");
        final StartupManager startupManager = new StartupManager(null, testGlobalProperties, null, null, null, null, null);
        startupManager.validateHubProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Hub Provider..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Using localhost because PUBLIC_HUB_WEBSERVER_HOST environment variable is set to localhost"));
    }

    @Test
    public void testValidateHubInvalidProvider() throws IOException {
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties();
        final StartupManager startupManager = new StartupManager(null, testGlobalProperties, null, null, null, null, null);
        testGlobalProperties.setHubUrl("https://localhost:443");
        startupManager.validateHubProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Hub Provider..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Invalid; cause:"));
    }

    @Test
    public void testValidateHubValidProvider() throws IOException {
        final TestGlobalProperties testGlobalProperties = new TestGlobalProperties();
        final StartupManager startupManager = new StartupManager(null, testGlobalProperties, null, null, null, null, null);
        startupManager.validateHubProvider();
        assertTrue(outputLogger.isLineContainingText("Validating Hub Provider..."));
        assertTrue(outputLogger.isLineContainingText("Hub Provider Valid!"));
    }
}
