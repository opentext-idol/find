package com.hp.autonomy.frontend.find.idol.aci;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import com.hp.autonomy.types.idol.IdolResponseParser;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AciResponseProcessorTest {
    @Mock
    private IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser;

    private AciResponseProcessor<GetStatusResponseData> aciResponseProcessor;

    @Before
    public void setUp() {
        aciResponseProcessor = new AciResponseProcessor<>(idolResponseParser, GetStatusResponseData.class);
    }

    @Test
    public void process() {
        aciResponseProcessor.process(new MockAciResponseInputStream(IOUtils.toInputStream("Some data")));
        verify(idolResponseParser).parseIdolResponseData(anyString(), any(Class.class));
    }

    @Test(expected = ProcessorException.class)
    public void errorReadingAciResponseStream() {
        aciResponseProcessor.process(mock(AciResponseInputStream.class));
    }

    private static class MockAciResponseInputStream extends AciResponseInputStream {
        /**
         * Creates a new instance of AciResponseInputStream.
         *
         * @param inputStream the ACI response
         */
        public MockAciResponseInputStream(final InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getHeader(final String name) {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public String getContentType() {
            return null;
        }
    }
}
