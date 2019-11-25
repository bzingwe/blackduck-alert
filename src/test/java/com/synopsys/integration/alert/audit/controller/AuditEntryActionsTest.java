package com.synopsys.integration.alert.audit.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.gson.Gson;
import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.web.audit.AuditEntryActions;
import com.synopsys.integration.exception.IntegrationException;

public class AuditEntryActionsTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testGetNull() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        Mockito.when(notificationManager.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, null, null, notificationManager, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, null, null, null);

        Optional<AuditEntryModel> auditEntryModel = auditEntryActions.get(1L);
        assertTrue(auditEntryModel.isEmpty());
    }

    @Test
    public void testGetAuditInfoForJobNull() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(Mockito.any())).thenReturn(Optional.empty());

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, null, null, null, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, null, null, null, null);

        Optional<AuditJobStatusModel> jobAuditModel = auditEntryActions.getAuditInfoForJob(UUID.randomUUID());
        assertTrue(jobAuditModel.isEmpty());
    }

    @Test
    public void testResendNotificationException() throws AlertDatabaseConstraintException {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        ConfigurationAccessor jobConfigReader = Mockito.mock(ConfigurationAccessor.class);
        MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        MockNotificationContent mockNotificationEntity = new MockNotificationContent();
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(mockAuditEntryEntity.createEmptyEntity()));
        Mockito.when(jobConfigReader.getJobById(Mockito.any())).thenReturn(null);
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(mockNotificationEntity.createEntity()));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationRepository, auditEntryRepository, auditNotificationRepository, eventManager);
        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, null);
        AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, jobConfigReader, null, null);

        AlertPagedModel<AuditEntryModel> restModel = null;
        try {
            restModel = auditEntryActions.resendNotification(1L, null);
            fail();
        } catch (IntegrationException e) {
        }

        assertNull(restModel);
    }

    @Test
    public void testPagedRequest() throws AlertDatabaseConstraintException {
        int totalPages = 2;
        int currentPage = 0;
        int pageSize = 2;

        NotificationContent entity_1 = new NotificationContent();
        entity_1.setId(1L);
        NotificationContent entity_2 = new NotificationContent();
        entity_2.setId(2L);
        List<AlertNotificationWrapper> pagedEntryList = Arrays.asList(entity_1, entity_2);
        @SuppressWarnings("unchecked") Page<AlertNotificationWrapper> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(pagedEntryList);
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(pageSize);

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.unsorted());
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationAccessor jobConfigReader = Mockito.mock(ConfigurationAccessor.class);

        NotificationContent notificationContent = new MockNotificationContent(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L).createEntity();
        ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

        ConfigurationModel configuration = MockConfigurationModelFactory.createCommonConfigModel(1L, 2L, "distributionType", "name", "providerName", "frequency",
            "filterByProject", "projectNamePattern", Collections.emptyList(), Collections.emptyList(), "formatType");

        Mockito.doReturn(Optional.of(configuration)).when(jobConfigReader).getJobById(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, contentConverter);
        AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, jobConfigReader, null, null);

        AlertPagedModel<AuditEntryModel> restModel = auditEntryActions.get(currentPage, pageSize, null, null, null, true);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        assertEquals(pageResponse.getSize(), restModel.getPageSize());

        for (int index = 0; index < pageSize; index++) {
            AlertNotificationWrapper entity = pageResponse.getContent().get(index);
            AuditEntryModel entryRestModel = restModel.getContent().get(index);
            assertEquals(String.valueOf(entity.getId()), entryRestModel.getId());
        }
    }

    @Test
    public void testPagedRequestEmptyList() throws AlertDatabaseConstraintException {
        int totalPages = 1;
        int currentPage = 1;
        int pageSize = 1;
        @SuppressWarnings("unchecked") Page<AlertNotificationWrapper> pageResponse = Mockito.mock(Page.class);

        Mockito.when(pageResponse.getContent()).thenReturn(Collections.emptyList());
        Mockito.when(pageResponse.getTotalPages()).thenReturn(totalPages);
        Mockito.when(pageResponse.getNumber()).thenReturn(currentPage);
        Mockito.when(pageResponse.getSize()).thenReturn(0);

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);

        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        Mockito.when(notificationManager.findAll(Mockito.any(PageRequest.class), Mockito.anyBoolean())).thenReturn(pageResponse);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.unsorted());
        Mockito.when(notificationManager.getPageRequestForNotifications(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(pageRequest);

        NotificationContentRepository notificationRepository = Mockito.mock(NotificationContentRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationAccessor jobConfigReader = Mockito.mock(ConfigurationAccessor.class);
        ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        NotificationContent notificationContent = new MockNotificationContent(new Date(), "provider", new Date(), "notificationType", "{content: \"content is here...\"}", 1L).createEntity();

        ConfigurationModel configuration = MockConfigurationModelFactory.createCommonConfigModel(1L, 2L, "distributionType", "name", "providerName", "frequency",
            "filterByProject", "projectNamePattern", Collections.emptyList(), Collections.emptyList(), "formatType");

        Mockito.doReturn(Optional.of(configuration)).when(jobConfigReader).getJobById(Mockito.any());
        Mockito.when(notificationRepository.findAllById(Mockito.anyList())).thenReturn(Collections.singletonList(notificationContent));

        DefaultAuditUtility auditEntryUtility = new DefaultAuditUtility(auditEntryRepository, auditNotificationRepository, jobConfigReader, notificationManager, contentConverter);
        AuditEntryActions auditEntryActions = new AuditEntryActions(auditEntryUtility, notificationManager, jobConfigReader, null, null);

        AlertPagedModel<AuditEntryModel> restModel = auditEntryActions.get(currentPage, pageSize, null, null, null, true);
        assertEquals(pageResponse.getTotalPages(), restModel.getTotalPages());
        assertEquals(pageResponse.getNumber(), restModel.getCurrentPage());
        //Assert 0 because there aren't any entries in the pageResponse content
        assertEquals(0, restModel.getPageSize());
        assertTrue(restModel.getContent().isEmpty());
    }

}
