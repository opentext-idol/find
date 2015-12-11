/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.Qs;
import com.hp.autonomy.types.idol.QsElement;
import com.hp.autonomy.types.idol.QueryResponseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolRelatedConceptsServiceTest {
    @Mock
    private AciService contentAciService;

    @Mock
    private AciResponseJaxbProcessorFactory aciResponseProcessorFactory;

    private IdolRelatedConceptsService idolRelatedConceptsService;

    @Before
    public void setUp() {
        idolRelatedConceptsService = new IdolRelatedConceptsService(contentAciService, aciResponseProcessorFactory);
    }

    @Test
    public void findRelatedConcepts() {
        final QueryResponseData responseData = new QueryResponseData();
        final Qs qs = new Qs();
        qs.getElement().add(new QsElement());
        responseData.setQs(qs);

        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenReturn(responseData);

        final List<QsElement> results = idolRelatedConceptsService.findRelatedConcepts("Some text", Collections.singletonList("Database 1"), "Some field text");
        assertThat(results, is(not(empty())));
    }
}
