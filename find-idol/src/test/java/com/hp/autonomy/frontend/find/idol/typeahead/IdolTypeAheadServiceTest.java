/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadService;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolTypeAheadServiceTest {
    @Mock
    private ConfigService<? extends IdolSearchCapable> configService;

    @Mock
    private TypeAheadService termExpandTypeAheadService;

    @Mock
    private TypeAheadService qmsTypeAheadService;

    @Mock
    private IdolSearchCapable config;

    private TypeAheadService typeAheadService;

    @Before
    public void setUp() {
        typeAheadService = new IdolTypeAheadService(configService, termExpandTypeAheadService, qmsTypeAheadService);
        when(configService.getConfig()).thenReturn(config);
    }

    @Test
    public void getQmsSuggestions() throws GetSuggestionsFailedException {
        when(config.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setEnabled(true).build());

        final String text = "A";
        typeAheadService.getSuggestions(text);
        verify(qmsTypeAheadService).getSuggestions(text);
    }

    @Test
    public void getContentSuggestions() throws GetSuggestionsFailedException {
        when(config.getQueryManipulation()).thenReturn(new QueryManipulation.Builder().setEnabled(false).build());

        final String text = "A";
        typeAheadService.getSuggestions(text);
        verify(termExpandTypeAheadService).getSuggestions(text);
    }
}
