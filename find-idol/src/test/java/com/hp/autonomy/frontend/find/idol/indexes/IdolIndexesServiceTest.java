package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorCallback;
import com.hp.autonomy.frontend.find.idol.aci.AciResponseProcessorFactory;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.Databases;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolIndexesServiceTest {
    @Mock
    private AciService contentAciService;

    @Mock
    private AciResponseProcessorFactory aciResponseProcessorFactory;

    @Mock
    private Processor<List<Database>> responseProcessor;

    @Mock
    private AciResponseInputStream aciResponseInputStream;

    private IdolIndexesService idolIndexesService;

    @Before
    public void setUp() {
        idolIndexesService = new IdolIndexesService(contentAciService, aciResponseProcessorFactory);
    }

    @Test
    public void listVisibleIndexes() {
        final List<AciResponseProcessorCallback<GetStatusResponseData, List<Database>>> callbacks = new ArrayList<>();
        when(aciResponseProcessorFactory.createAciResponseProcessor(any(Class.class), any(AciResponseProcessorCallback.class))).thenAnswer(new Answer<Processor<List<Database>>>() {
            @Override
            public Processor<List<Database>> answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                callbacks.add((AciResponseProcessorCallback<GetStatusResponseData, List<Database>>) invocationOnMock.getArguments()[1]);
                return responseProcessor;
            }
        });
        when(responseProcessor.process(aciResponseInputStream)).thenAnswer(new Answer<List<Database>>() {
            @Override
            public List<Database> answer(final InvocationOnMock invocationOnMock) {
                final GetStatusResponseData responseData = new GetStatusResponseData();
                final Databases databases = new Databases();
                databases.getDatabase().add(new Database());
                responseData.setDatabases(databases);
                return callbacks.get(0).process(responseData);
            }
        });

        when(contentAciService.executeAction(anySetOf(AciParameter.class), eq(responseProcessor))).thenAnswer(new Answer<List<Database>>() {
            @Override
            public List<Database> answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                return ((Processor<List<Database>>) invocationOnMock.getArguments()[1]).process(aciResponseInputStream);
            }
        });

        assertThat(idolIndexesService.listVisibleIndexes(), is(not(empty())));
    }
}
