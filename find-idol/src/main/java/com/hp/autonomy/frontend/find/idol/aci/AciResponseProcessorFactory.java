package com.hp.autonomy.frontend.find.idol.aci;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.hp.autonomy.types.idol.IdolResponseParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AciResponseProcessorFactory {
    private final IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser;

    @Autowired
    public AciResponseProcessorFactory(final IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser) {
        this.idolResponseParser = idolResponseParser;
    }

    public <T, R> Processor<R> createAciResponseProcessor(final Class<T> responseDataType, final AciResponseProcessorCallback<T, R> callback) {
        return new AciResponseProcessor<>(idolResponseParser, responseDataType, callback);
    }
}
