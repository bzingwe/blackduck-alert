package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.mock.MockProviderDataAccessor;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.ResourceMetadata;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectUsersService;

public class BlackDuckProjectSyncTaskTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Test
    public void testRun() throws Exception {
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        MockProviderDataAccessor providerDataAccessor = new MockProviderDataAccessor();

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
        configurationFieldModel.setFieldValues(List.of("project", "project2"));
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, null, null, ConfigContextEnum.DISTRIBUTION);
        configurationModel.put(configurationFieldModel);
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(UUID.randomUUID(), Set.of(configurationModel));
        Mockito.when(configurationAccessor.getAllJobs()).thenReturn(List.of(configurationJobModel));

        String email1 = "user1@email.com";
        String email2 = "user2@email.com";
        String email3 = "user3@email.com";
        String email4 = "user4@email.com";

        Mockito.when(blackDuckProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any())).thenReturn(Optional.of(Mockito.mock(BlackDuckHttpClient.class)));
        BlackDuckServicesFactory BlackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(BlackDuckServicesFactory);

        BlackDuckService hubService = Mockito.mock(BlackDuckService.class);
        Mockito.when(BlackDuckServicesFactory.createBlackDuckService()).thenReturn(hubService);

        ProjectUsersService projectUsersService = Mockito.mock(ProjectUsersService.class);
        Mockito.when(BlackDuckServicesFactory.createProjectUsersService()).thenReturn(projectUsersService);

        ProjectView projectView = createProjectView("project", "description1", "projectUrl1");
        ProjectView projectView2 = createProjectView("project2", "description2", "projectUrl2");
        ProjectView projectView3 = createProjectView("project3", "description3", "projectUrl3");

        Mockito.when(hubService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2, projectView3));
        Mockito.doReturn(null).when(hubService).getResponse(Mockito.any(BlackDuckPathSingleResponse.class));

        UserView user1 = createUserView(email1, true);
        UserView user2 = createUserView(email2, true);
        UserView user3 = createUserView(email3, true);
        UserView user4 = createUserView(email4, true);

        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(Arrays.asList(user2, user4)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(Arrays.asList(user3)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView3))).thenReturn(new HashSet<>(Arrays.asList(user1, user2, user3)));
        Mockito.doNothing().when(projectUsersService).addUserToProject(Mockito.any(), Mockito.any(UserView.class));

        BlackDuckProjectSyncTask projectSyncTask = new BlackDuckProjectSyncTask(null, blackDuckProperties, providerDataAccessor, configurationAccessor, BLACK_DUCK_PROVIDER_KEY);
        projectSyncTask.run();

        assertEquals(3, providerDataAccessor.findByProviderName(BLACK_DUCK_PROVIDER_KEY.getUniversalKey()).size());

        Mockito.when(hubService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2));

        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(Arrays.asList(user2, user4)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(Arrays.asList(user3)));

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Collections.emptyList());
        projectSyncTask.run();

        assertEquals(2, providerDataAccessor.findByProviderName(BLACK_DUCK_PROVIDER_KEY.getUniversalKey()).size());
    }

    public UserView createUserView(String email, Boolean active) {
        UserView userView = new UserView();
        userView.setEmail(email);
        userView.setActive(active);
        return userView;
    }

    public ProjectView createProjectView(String name, String description, String href) {
        ProjectView projectView = new ProjectView();
        projectView.setName(name);
        projectView.setDescription(description);
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(href);
        projectView.setMeta(resourceMetadata);
        return projectView;
    }

}
