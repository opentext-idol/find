package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciParameter;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.frontend.find.core.indexes.IndexesService;
import com.hp.autonomy.types.idol.Database;
import com.hp.autonomy.types.idol.GetStatusResponseData;
import com.hp.autonomy.types.idol.IdolResponseParser;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class IdolIndexesService implements IndexesService<Database, AciErrorException> {
    private final AciService contentAciService;
    private final IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser;

    @Autowired
    public IdolIndexesService(final AciService contentAciService, final IdolResponseParser<AciErrorException, ProcessorException> idolResponseParser) {
        this.contentAciService = contentAciService;
        this.idolResponseParser = idolResponseParser;
    }

    @SuppressWarnings("SerializableInnerClassWithNonSerializableOuterClass")
    @Override
    public List<Database> listVisibleIndexes() throws AciErrorException {
        return contentAciService.executeAction(Collections.singleton(new AciParameter("action", "getstatus")), new Processor<List<Database>>() {
            private static final long serialVersionUID = -1983490659468698548L;

            @Override
            public List<Database> process(final AciResponseInputStream aciResponseInputStream) {
                final String xml;
                try {
                    xml = IOUtils.toString(aciResponseInputStream);
                } catch (final IOException e) {
                    throw new ProcessorException("Error running getstatus command", e);
                }

                final GetStatusResponseData responseData = idolResponseParser.parseIdolResponseData(xml, GetStatusResponseData.class);
                return responseData.getDatabases().getDatabase();
            }
        });
    }
}
