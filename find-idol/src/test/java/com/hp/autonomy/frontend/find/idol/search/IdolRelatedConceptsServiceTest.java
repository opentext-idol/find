package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorCallback;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import com.hp.autonomy.types.idol.Qs;
import com.hp.autonomy.types.idol.QsElement;
import com.hp.autonomy.types.idol.QueryResponseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
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

    @Mock
    private Processor<List<QsElement>> queryResponseProcessor;

    @Mock
    private AciResponseInputStream aciResponseInputStream;

    @Before
    public void setUp() {
        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                return ((Processor<?>) invocationOnMock.getArguments()[1]).process(aciResponseInputStream);
            }
        });
    }

    @Test
    public void findRelatedConcepts() {
        final Deque<AciResponseProcessorCallback<QueryResponseData, List<QsElement>>> callbacks = new ArrayDeque<>(1);
        when(aciResponseProcessorFactory.createAciResponseProcessor(any(Class.class), any(AciResponseProcessorCallback.class))).thenAnswer(new Answer<Processor<List<QsElement>>>() {
            @Override
            public Processor<List<QsElement>> answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                callbacks.add((AciResponseProcessorCallback<QueryResponseData, List<QsElement>>) invocationOnMock.getArguments()[1]);
                return queryResponseProcessor;
            }
        });

        when(queryResponseProcessor.process(aciResponseInputStream)).thenAnswer(new Answer<List<QsElement>>() {
            @Override
            public List<QsElement> answer(final InvocationOnMock invocationOnMock) {
                final QueryResponseData responseData = new QueryResponseData();
                final Qs qs = new Qs();
                qs.getElement().add(new QsElement());
                responseData.setQs(qs);
                //noinspection unchecked
                return callbacks.pop().process(responseData);
            }
        });

        final IdolRelatedConceptsService idolRelatedConceptsService = new IdolRelatedConceptsService(contentAciService, aciResponseProcessorFactory);

        final List<QsElement> results = idolRelatedConceptsService.findRelatedConcepts("Some text", Collections.<DatabaseName>emptyList(), "Some field text");
        assertThat(results, is(not(empty())));
    }
}
