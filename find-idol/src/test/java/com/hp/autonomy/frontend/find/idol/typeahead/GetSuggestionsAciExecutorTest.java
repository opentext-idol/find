/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GetSuggestionsAciExecutorTest {
    @Mock
    private AciService aciService;

    @Mock
    private Processor<?> processor;

    private GetSuggestionsAciExecutor getSuggestionsAciExecutor;

    @Before
    public void setUp() {
        getSuggestionsAciExecutor = new GetSuggestionsAciExecutorImpl();
    }

    @Test
    public void executeAction() throws GetSuggestionsFailedException {
        final Set<AciParameter> aciParameters = new AciParameters();
        getSuggestionsAciExecutor.executeAction(aciService, processor, aciParameters);
        verify(aciService).executeAction(aciParameters, processor);
    }

    @Test(expected = GetSuggestionsFailedException.class)
    public void errorInIdolResponse() throws GetSuggestionsFailedException {
        doThrow(new AciErrorException()).when(aciService).executeAction(anySetOf(AciParameter.class), any(Processor.class));
        getSuggestionsAciExecutor.executeAction(aciService, processor, new AciParameters());
    }
}
