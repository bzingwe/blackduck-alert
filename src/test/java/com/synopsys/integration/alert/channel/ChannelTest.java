/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.util.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.util.RestChannelUtility;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.alert.util.OutputLogger;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.web.component.settings.DefaultProxyManager;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public abstract class ChannelTest {
    protected Gson gson;
    protected TestProperties properties;
    protected OutputLogger outputLogger;
    protected ContentConverter contentConverter;

    @BeforeEach
    public void init() throws IOException {
        gson = new Gson();
        properties = new TestProperties();
        outputLogger = new OutputLogger();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
    }

    @AfterEach
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    public ProviderMessageContent createMessageContent(final String testName) throws AlertException {
        final LinkableItem linkableItem1 = new LinkableItem("First Linkable Item", "Value 1", "https://google.com");
        final LinkableItem linkableItem2 = new LinkableItem("Second Linkable Item", "Value 2", "https://google.com");

        final String nameKey = "Same Key";
        final LinkableItem linkableItem3 = new LinkableItem(nameKey, "Value", "https://google.com");
        final LinkableItem linkableItem4 = new LinkableItem(nameKey, "No Link Value");
        final LinkableItem linkableItem5 = new LinkableItem(nameKey, "Other Value", "https://google.com");

        ComponentItem.Builder componentBuilder1 = new ComponentItem.Builder();
        ComponentItem.Builder componentBuilder2 = new ComponentItem.Builder();
        ComponentItem.Builder componentBuilder3 = new ComponentItem.Builder();

        componentBuilder1
            .applyComponentData("component", "componentValue")
            .applyCategoryItem("categoryItem", "categoryValue")
            .applyCategory("category")
            .applyNotificationId(1L)
            .applyOperation(ItemOperation.ADD)
            .applyComponentAttribute(linkableItem1)
            .applyComponentAttribute(linkableItem2);

        componentBuilder2
            .applyComponentData("component", "componentValue")
            .applyCategoryItem("categoryItem", "categoryValue")
            .applyCategory("category")
            .applyNotificationId(2L)
            .applyOperation(ItemOperation.UPDATE)
            .applyComponentAttribute(linkableItem2);

        componentBuilder3
            .applyComponentData("component", "componentValue")
            .applyCategoryItem("categoryItem", "categoryValue")
            .applyCategory("category")
            .applyNotificationId(1L)
            .applyOperation(ItemOperation.DELETE)
            .applyComponentAttribute(linkableItem3)
            .applyComponentAttribute(linkableItem4)
            .applyComponentAttribute(linkableItem5);

        final LinkableItem subTopic = new LinkableItem("Sub Topic", "Sub Topic Value", "https://google.com");

        final Collection<ComponentItem> items = new LinkedList<>();
        items.add(componentBuilder1.build());
        items.add(componentBuilder2.build());
        items.add(componentBuilder3.build());

        ProviderMessageContent.Builder providerBuilder = new ProviderMessageContent.Builder();
        providerBuilder
            .applyProvider("Test Provider")
            .applyTopic("Topic", testName, "https://google.com")
            .applySubTopic(subTopic.getName(), subTopic.getValue(), subTopic.getUrl().orElse(null))
            .applyAllComponentItems(items);

        return providerBuilder.build();
    }

    public AuditUtility createAuditUtility() {
        return Mockito.mock(DefaultAuditUtility.class);
    }

    public RestChannelUtility createRestChannelUtility() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final DefaultProxyManager defaultProxyManager = Mockito.mock(DefaultProxyManager.class);
        Mockito.when(defaultProxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, defaultProxyManager);
        return new RestChannelUtility(channelRestConnectionFactory);
    }

    private SortedSet<LinkableItem> asSet(final LinkableItem... items) {
        final SortedSet<LinkableItem> collection = new TreeSet<>();
        for (final LinkableItem item : items) {
            collection.add(item);
        }
        return collection;
    }
}
