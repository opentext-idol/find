package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
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
    private AciResponseProcessorFactory aciResponseProcessorFactory;

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

        final List<QsElement> results = idolRelatedConceptsService.findRelatedConcepts("Some text", Collections.<String>emptyList(), "Some field text");
        assertThat(results, is(not(empty())));
    }
}
