/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.stats;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.stats.AbandonmentEvent;
import com.hp.autonomy.frontend.find.core.stats.ClickThroughEvent;
import com.hp.autonomy.frontend.find.core.stats.ClickType;
import com.hp.autonomy.frontend.find.core.stats.Event;
import com.hp.autonomy.frontend.find.core.stats.PageEvent;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.StatsServerConfig;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xmlunit.builder.Input;
import org.xmlunit.input.WhitespaceStrippedSource;
import org.xmlunit.matchers.HasXPathMatcher;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

@RunWith(MockitoJUnitRunner.class)
public class IdolStatsServiceTest {

    @Mock
    private AciService aciService;

    @Mock
    private AciResponseJaxbProcessorFactory processorFactory;

    @Mock
    private AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever;

    @Mock
    private ConfigService<IdolFindConfig> configService;

    @Mock
    private IdolFindConfig config;

    @Mock
    private StatsServerConfig statsServerConfig;

    private IdolStatsService statsService;

    @Before
    public void setUp() {
        final XmlMapper xmlMapper = new XmlMapper();

        when(configService.getConfig()).thenReturn(config);
        when(config.getStatsServer()).thenReturn(statsServerConfig);
        when(statsServerConfig.isEnabled()).thenReturn(true);

        statsService = new IdolStatsService(aciService, processorFactory, xmlMapper, configService);
    }

    @Test
    public void testSimpleEvent() {
        final SimpleEvent event = new SimpleEvent("Steve", 123456789L);
        final SimpleEvent event2 = new SimpleEvent("Bob", 31415926L);

        final String xml = submitEvent(Arrays.asList(event, event2));

        final InputStream expectedStream = getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/idol/stats/simple-events.xml");

        final Source expectedFile = new WhitespaceStrippedSource(Input.fromStream(expectedStream).build());

        assertThat(xml, isIdenticalTo(expectedFile));
    }

    @Test
    public void testPageEvent() throws IOException {
        when(authenticationInformationRetriever.getPrincipal()).thenReturn(new TestPrincipal("Bear Man"));

        final Event event = new PageEvent("bears", 1, authenticationInformationRetriever);

        final String xml = submitEvent(event);

        // can't use files as we don't know the timestamps
        assertThat(xml, hasXPath("events/find-event/search", equalTo("bears")));
        assertThat(xml, hasXPath("events/find-event/page", equalTo("1")));
        assertThat(xml, hasXPath("events/find-event/event", equalTo("Find")));
        assertThat(xml, hasXPath("events/find-event/type", equalTo("page")));

        assertThat(xml, HasXPathMatcher.hasXPath("events/find-event/timestamp"));
    }

    @Test
    public void testAbandonmentEvent() throws IOException {
        when(authenticationInformationRetriever.getPrincipal()).thenReturn(new TestPrincipal("Cat Man"));

        final Event event = new AbandonmentEvent("cats", ClickType.original, authenticationInformationRetriever);

        final String xml = submitEvent(event);

        assertThat(xml, hasXPath("events/find-event/search", equalTo("cats")));
        assertThat(xml, hasXPath("events/find-event/click-type", equalTo("original")));
        assertThat(xml, hasXPath("events/find-event/type", equalTo("abandonment")));
        assertThat(xml, hasXPath("events/find-event/event", equalTo("Find")));

        assertThat(xml, HasXPathMatcher.hasXPath("events/find-event/timestamp"));
    }

    @Test
    public void testClickThroughEvent() throws IOException {
        when(authenticationInformationRetriever.getPrincipal()).thenReturn(new TestPrincipal("Batman"));

        final Event event = new ClickThroughEvent("bats", ClickType.preview, 1, authenticationInformationRetriever);

        final String xml = submitEvent(event);

        assertThat(xml, hasXPath("events/find-event/search", equalTo("bats")));
        assertThat(xml, hasXPath("events/find-event/click-type", equalTo("preview")));
        assertThat(xml, hasXPath("events/find-event/type", equalTo("clickthrough")));
        assertThat(xml, hasXPath("events/find-event/position", equalTo("1")));
        assertThat(xml, hasXPath("events/find-event/event", equalTo("Find")));
        assertThat(xml, HasXPathMatcher.hasXPath("events/find-event/timestamp"));
    }

    private String submitEvent(final Event event) {
        return submitEvent(Collections.singletonList(event));
    }

    private String submitEvent(final List<? extends Event> events) {
        for (final Event event : events) {
            statsService.recordEvent(event);
        }

        statsService.drainQueue();

        final ArgumentCaptor<AciParameters> captor = ArgumentCaptor.forClass(AciParameters.class);

        verify(aciService).executeAction(captor.capture(), Matchers.<Processor<Object>>anyObject());

        return captor.getValue().get("data");
    }

    private static class TestPrincipal implements Principal {

        private final String name;

        private TestPrincipal(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Data
    @JacksonXmlRootElement(localName = "simple-event")
    private static class SimpleEvent implements Event {
        private final String username;
        private final long timestamp;

        @Override
        public String getType() {
            return "simple";
        }
    }
}