package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.IdolResponseParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdolIndexesServiceTest {
    @Mock
    private AciService contentAciService;

    @Mock
    private IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser;

    @Mock
    private AciResponseInputStream aciResponseInputStream;

    private IdolIndexesService idolIndexesService;

    @Before
    public void setUp() {
        idolIndexesService = new IdolIndexesService(contentAciService, idolResponseParser);
    }

    @Test(expected = ProcessorException.class)
    public void errorReadingAciResponseStream() {
        when(contentAciService.executeAction(anySetOf(AciParameter.class), any(Processor.class))).thenAnswer(new Answer<List<Database>>() {
            @Override
            public List<Database> answer(final InvocationOnMock invocationOnMock) {
                //noinspection unchecked
                return ((Processor<List<Database>>) invocationOnMock.getArguments()[1]).process(aciResponseInputStream);
            }
        });
        idolIndexesService.listVisibleIndexes();
    }
}
