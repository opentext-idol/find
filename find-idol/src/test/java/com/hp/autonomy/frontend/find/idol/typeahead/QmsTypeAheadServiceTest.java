/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.types.idol.TypeAheadResponseData;
import com.hp.autonomy.types.requests.qms.actions.typeahead.params.ModeParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QmsTypeAheadServiceTest {
    @Mock
    private ConfigService<? extends IdolSearchCapable> configService;

    @Mock
    private GetSuggestionsAciExecutor executor;

    @Mock
    private AciService qmsAciService;

    @Mock
    private AciResponseJaxbProcessorFactory processorFactory;

    @Mock
    private IdolSearchCapable config;

    private QmsTypeAheadService qmsTypeAheadService;

    @Before
    public void setUp() {
        qmsTypeAheadService = new QmsTypeAheadService(configService, executor, qmsAciService, processorFactory);
        when(configService.getConfig()).thenReturn(config);
    }

    @Test
    public void getSuggestionsInDictionaryMode() throws GetSuggestionsFailedException {
        when(config.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setTypeAheadMode(ModeParam.Dictionary).build());
        when(executor.executeAction(any(AciService.class), any(Processor.class), anySetOf(AciParameter.class))).thenReturn(mockResponse());
        final List<String> suggestions = qmsTypeAheadService.getSuggestions("A");
        assertEquals("Ab", suggestions.get(0));
    }

    @Test
    public void getSuggestionsInIndexMode() throws GetSuggestionsFailedException {
        when(config.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setTypeAheadMode(ModeParam.Index).build());
        when(executor.executeAction(any(AciService.class), any(Processor.class), anySetOf(AciParameter.class))).thenReturn(mockResponse());
        final List<String> suggestions = qmsTypeAheadService.getSuggestions("A");
        assertEquals("ab", suggestions.get(0));
    }

    private TypeAheadResponseData mockResponse() {
        final TypeAheadResponseData typeAheadResponseData = new TypeAheadResponseData();
        final TypeAheadResponseData.Expansion expansion = new TypeAheadResponseData.Expansion();
        expansion.setScore(5);
        expansion.setValue("Ab");
        typeAheadResponseData.getExpansion().add(expansion);
        return typeAheadResponseData;
    }
}
