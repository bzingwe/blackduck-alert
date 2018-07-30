package com.blackducksoftware.integration.alert;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.web.channel.model.SlackDistributionConfig;
import com.google.gson.Gson;

public class ContentConverterTest {

    @Test
    public void testLong() {
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final long longActual = contentConverter.getLongValue("1");

        assertEquals(1, longActual);
    }

    @Test
    public void testGetContent() {
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final MockSlackRestModel mockSlackRestModel = new MockSlackRestModel();

        final SlackDistributionConfig restModel = mockSlackRestModel.createRestModel();

        final String restModelJson = restModel.toString();
        final SlackDistributionConfig restModelActual = contentConverter.getJsonContent(restModelJson, SlackDistributionConfig.class);

        assertEquals(restModel, restModelActual);
    }
}
