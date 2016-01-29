/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.TermExpandResponseData;
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
public class TermExpandTypeAheadServiceTest {
    @Mock
    private GetSuggestionsAciExecutor executor;

    @Mock
    private AciService contentAciService;

    @Mock
    private AciResponseJaxbProcessorFactory processorFactory;

    private TermExpandTypeAheadService termExpandTypeAheadService;

    @Before
    public void setUp() {
        termExpandTypeAheadService = new TermExpandTypeAheadService(executor, contentAciService, processorFactory);
    }

    @Test
    public void getSuggestions() throws GetSuggestionsFailedException {
        when(executor.executeAction(any(AciService.class), any(Processor.class), anySetOf(AciParameter.class))).thenReturn(mockResponse());
        final List<String> suggestions = termExpandTypeAheadService.getSuggestions("A");
        assertEquals("ab", suggestions.get(0));
    }

    private TermExpandResponseData mockResponse() {
        final TermExpandResponseData typeAheadResponseData = new TermExpandResponseData();
        final TermExpandResponseData.Term term = new TermExpandResponseData.Term();
        term.setValue("Ab");
        typeAheadResponseData.getTerm().add(term);
        return typeAheadResponseData;
    }
}
