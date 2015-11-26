package com.hp.autonomy.frontend.find.idol.aci;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import com.hp.autonomy.types.idol.IdolResponseParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class AciResponseProcessorFactoryTest {
    @Mock
    private IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser;

    private AciResponseProcessorFactory aciResponseProcessorFactory;

    @Before
    public void setUp() {
        aciResponseProcessorFactory = new AciResponseProcessorFactory(idolResponseParser);
    }

    @Test
    public void createAciResponseProcessor() {
        assertNotNull(aciResponseProcessorFactory.createAciResponseProcessor(GetStatusResponseData.class));
    }
}
