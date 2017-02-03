/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.services.impl.ErrorProcessor;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.autonomy.aci.client.transport.impl.HttpClientFactory;
import com.autonomy.aci.client.util.AciParameters;
import com.autonomy.aci.client.util.AciURLCodec;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.impl.DreAddDataCommand;
import com.autonomy.nonaci.indexing.impl.IndexingServiceImpl;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestIndex;
import com.hp.autonomy.searchcomponents.idol.search.IdolGetContentRequestIndexBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.IdolSuggestRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolSuggestRequestBuilder;
import com.hp.autonomy.types.idol.marshalling.processors.NoopProcessor;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import java.io.IOException;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
class IdolDocumentsController extends DocumentsController<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, String, IdolQueryRestrictions, IdolGetContentRequestIndex, IdolSearchResult, AciErrorException> {
    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    public IdolDocumentsController(final IdolDocumentsService documentsService,
                                   final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                   final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory,
                                   final ObjectFactory<IdolSuggestRequestBuilder> suggestRequestBuilderFactory,
                                   final ObjectFactory<IdolGetContentRequestBuilder> getContentRequestBuilderFactory,
                                   final ObjectFactory<IdolGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory) {
        super(documentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);
    }

    // For a proper demo, you'd want to add these to the constructor
    @Autowired
    private AciServiceRetriever aciServiceRetriever;
    @Autowired
    private DocumentFieldsService documentFieldsService;

    @Override
    protected <T> T throwException(final String message) throws AciErrorException {
        throw new AciErrorException(message);
    }

    @Override
    protected void addParams(final GetContentRequestBuilder<IdolGetContentRequest, IdolGetContentRequestIndex, ?> request) {
        ((IdolGetContentRequestBuilder) request)
                .print(PrintParam.All);
    }

    @RequestMapping(value = "edit-document", method = RequestMethod.POST)
    @ResponseBody
    public Boolean editDocument(
            @RequestParam("reference") final String reference,
            @RequestParam("database") final String database,
            @RequestParam("field") final String field,
            @RequestParam("value") final String value
    ) throws IOException, AciHttpException {
        // Check that the user is actually allowed to edit this field
        final Set<String> idolFields = documentFieldsService.getEditableIdolFields(field);

        if (idolFields.isEmpty()) {
            throw new IllegalArgumentException("Specified field is not editable");
        }

        final String firstIdolField = idolFields.iterator().next();


        final String encodedRef = AciURLCodec.getInstance().encode(reference);
        final AciParameters params = new AciParameters("getcontent");
        params.add("reference", encodedRef);
        params.add("databasematch", database);

        final AciService aciService = aciServiceRetriever.getAciService(QueryRequest.QueryType.RAW);

        final String idx = aciService.executeAction(params, new AbstractStAXProcessor<String>() {
            @Override
            public String process(final XMLStreamReader aciResponse) {
                try {
                    if(isErrorResponse(aciResponse)) {
                        setErrorProcessor(new ErrorProcessor());
                        processErrorResponse(aciResponse);
                    }

                    while(aciResponse.hasNext()) {
                        final int eventType = aciResponse.next();

                        if(XMLEvent.START_ELEMENT == eventType) {
                            if("autn:hit".equals(aciResponse.getLocalName())) {
                                return parseHit(aciResponse);
                            }
                        }
                    }
                } catch(final XMLStreamException | IOException e) {
                    throw new ProcessorException("Error parsing data", e);
                }
                return null;
            }

            private String parseHit(final XMLStreamReader aciResponse) throws XMLStreamException, IOException {
                int eventType;

                final StringBuilder builder = new StringBuilder();

                boolean inDOCUMENT = false, fieldPresent = false;

                while(aciResponse.hasNext() && !((eventType = aciResponse.next()) == XMLEvent.END_ELEMENT && "autn:hit".equals(aciResponse.getLocalName()))) {
                    if(XMLEvent.START_ELEMENT == eventType) {
                        final String nodeName = aciResponse.getLocalName();

                        if ("autn:reference".equals(nodeName)) {
                            builder.append("#DREREFERENCE ").append(aciResponse.getElementText()).append('\n');
                        }
                        else if ("autn:title".equals(nodeName)) {
                            builder.append("#DRETITLE ").append(aciResponse.getElementText()).append('\n');
                        }
                        else if("autn:content".equals(nodeName)) {
                            // skip to DOCUMENT tag
                            while (!(eventType == XMLEvent.START_ELEMENT && aciResponse.getLocalName().equals("DOCUMENT"))) {
                                eventType = aciResponse.next();
                            }

                            inDOCUMENT = true;
                        }
                        else if (inDOCUMENT) {
                            if("DRECONTENT".equals(nodeName)) {
                                if (!fieldPresent && StringUtils.isNotBlank(value)) {
                                    builder.append("#DREFIELD ").append(firstIdolField).append("=\"").append(value).append("\"\n");
                                }

                                builder.append("#DRECONTENT\n").append(aciResponse.getElementText()).append("\n#DREENDDOC\n");
                                break;
                            }
                            else {
                                if(!idolFields.contains(nodeName)) {
                                    builder.append("#DREFIELD ").append(nodeName).append("=\"").append(aciResponse.getElementText()).append("\"\n");
                                }
                                else if (!fieldPresent && StringUtils.isNotBlank(value)) {
                                    fieldPresent = true;
                                    builder.append("#DREFIELD ").append(nodeName).append("=\"").append(value).append("\"\n");
                                }
                            }
                        }
                    }
                }

                return builder.append("\n#DREENDDATANOOP\n\n").toString();
            }
        });

        if (StringUtils.isEmpty(idx)) {
            // Can't find the document, it's an error
            return false;
        }

        // We need to DREADD this into the target engine
        final String tgtHost = System.getProperty("content.index.host", "localhost");
        final int tgtPort = Integer.valueOf(System.getProperty("content.index.port", "9011"));

        final DreAddDataCommand command = new DreAddDataCommand();
        command.setDreDbName(database);
        command.put("CreateDatabase", "true");
        command.setKillDuplicates("reference");
        command.setPostData(idx);
        final HttpClient client = new HttpClientFactory().createInstance();
        new IndexingServiceImpl(new ServerDetails(tgtHost, tgtPort), client).executeCommand(command);

        final String srcHost = System.getProperty("content.source.host", "localhost");
        final int srcPort = Integer.valueOf(System.getProperty("content.source.port", "9000"));

        final AciParameters deleteParams = new AciParameters("query");
        deleteParams.put("text", "*");
        deleteParams.put("databasematch", database);
        deleteParams.put("matchreference", encodedRef);
        deleteParams.put("delete", "true");
        deleteParams.put("maxresults", 1);

        new AciServiceImpl(new AciHttpClientImpl(client), new AciServerDetails(srcHost, srcPort)).executeAction(deleteParams, new NoopProcessor());
        return true;
    }
}
