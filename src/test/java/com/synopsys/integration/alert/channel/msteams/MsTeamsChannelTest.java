package com.synopsys.integration.alert.channel.msteams;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.ChannelTest;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class MsTeamsChannelTest extends ChannelTest {
    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void sendMessageTestIT() throws IOException, IntegrationException {
        final FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(new TestAlertProperties());
        final MsTeamsEventParser msTeamsEventParser = new MsTeamsEventParser(freemarkerTemplatingService);
        final MsTeamsKey msTeamsKey = new MsTeamsKey();
        final MsTeamsChannel msTeamsChannel = new MsTeamsChannel(msTeamsKey, gson, createAuditUtility(), createRestChannelUtility(), msTeamsEventParser);
        final ProviderMessageContent messageContent = createMessageContent(getClass().getSimpleName() + ": Request");

        final Map<String, ConfigurationFieldModel> fieldModels = new HashMap<>();
        addConfigurationFieldToMap(fieldModels, MsTeamsDescriptor.KEY_WEBHOOK, properties.getProperty(TestPropertyKey.TEST_MSTEAMS_WEBHOOK));

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldModels);
        final DistributionEvent event = new DistributionEvent(
            "1L", msTeamsKey.getUniversalKey(), RestConstants.formatDate(new Date()), new BlackDuckProviderKey().getUniversalKey(), FormatType.DEFAULT.name(), MessageContentGroup.singleton(messageContent), fieldAccessor);

        msTeamsChannel.sendAuditedMessage(event);

        final boolean actual = outputLogger.isLineContainingText("Successfully sent a " + msTeamsKey.getUniversalKey() + " message!");
        assertTrue(actual, "No success message appeared in the logs");
    }

}
