package com.hp.autonomy.frontend.find.idol.aci;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.types.idol.IdolResponseParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Generic processor for handling Idol responses.
 * Note that this uses DOM processing behind the scenes so should not be used for very large responses.
 */
public class AciResponseProcessor<T, R> implements Processor<R> {
    private static final long serialVersionUID = -1983490659468698548L;

    private final IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser;
    private final AciResponseProcessorCallback<T, R> callback;
    private final Class<T> responseDataType;

    public AciResponseProcessor(final IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser, final Class<T> responseDataType, final AciResponseProcessorCallback<T, R> callback) {
        this.idolResponseParser = idolResponseParser;
        this.callback = callback;
        this.responseDataType = responseDataType;
    }

    @Override
    public R process(final AciResponseInputStream aciResponseInputStream) {
        final String xml;
        try {
            xml = IOUtils.toString(aciResponseInputStream);
        } catch (final IOException e) {
            throw new ProcessorException("Error running getstatus command", e);
        }

        final T responseData = idolResponseParser.parseIdolResponseData(xml, responseDataType);
        return callback.process(responseData);
    }
}
