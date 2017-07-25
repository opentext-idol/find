/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.impl.HttpClientFactory;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.impl.DreReplaceCommand;
import com.autonomy.nonaci.indexing.impl.IndexingServiceImpl;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
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
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import java.io.IOException;
import java.util.Set;
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
            @RequestParam(value = "value", defaultValue = "") final String value
    ) throws IOException, AciHttpException {
        // Check that the field is editable
        // TODO: permissions check on user roles
        final Set<String> idolFields = documentFieldsService.getEditableIdolFields(field);

        if (idolFields.isEmpty()) {
            throw new IllegalArgumentException("Specified field is not editable");
        }

        if (field.contains("\n") || value.contains("\n")) {
            throw new IllegalArgumentException("Illegal characters in field name / value");
        }

        // We need to DREREPLACE into the target engine
        final String tgtHost = System.getProperty("content.index.host", "localhost");
        final int tgtPort = Integer.valueOf(System.getProperty("content.index.port", "9001"));

        final StringBuilder idx = new StringBuilder("#DREDOCREF " + reference + '\n');
        final boolean isBlank = value.isEmpty();

        for(final String idolField : idolFields) {
            if (isBlank) {
                idx.append("#DREDELETEFIELD ").append(idolField).append("\n");
            }
            else {
                idx.append("#DREFIELDNAME ").append(idolField).append("\n");
                idx.append("#DREFIELDVALUE ").append(value).append("\n");
            }
        }

        idx.append("#DREENDDATANOOP\n\n");

        final DreReplaceCommand command = new DreReplaceCommand();
        command.setMultipleValues(false);
        command.setInsertValue(false);
        command.setPostData(idx.toString());
        command.setDatabaseMatch(database);
        final HttpClient client = new HttpClientFactory().createInstance();
        new IndexingServiceImpl(new ServerDetails(tgtHost, tgtPort), client).executeCommand(command);


        return true;
    }
}
