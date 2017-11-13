/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciHttpException;
import com.autonomy.aci.client.transport.impl.HttpClientFactory;
import com.autonomy.aci.client.util.AciParameters;
import com.autonomy.aci.client.util.AciURLCodec;
import com.autonomy.nonaci.ServerDetails;
import com.autonomy.nonaci.indexing.impl.DreReplaceCommand;
import com.autonomy.nonaci.indexing.impl.DreSyncCommand;
import com.autonomy.nonaci.indexing.impl.IndexingServiceImpl;
import com.hp.autonomy.aci.content.printfields.PrintFields;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.fields.DocumentFieldsService;
import com.hp.autonomy.searchcomponents.idol.configuration.AciServiceRetriever;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
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
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.DocContent;
import com.hp.autonomy.types.idol.responses.GetContentResponseData;
import com.hp.autonomy.types.idol.responses.Hit;
import com.hp.autonomy.types.requests.idol.actions.query.QueryActions;
import com.hp.autonomy.types.requests.idol.actions.query.params.GetContentParams;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
                                   final ObjectFactory<IdolGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory,
                                   final ProcessorFactory processorFactory) {
        super(documentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);

        getContentResponseProcessor = processorFactory.getResponseDataProcessor(GetContentResponseData.class);
    }

    // For a proper demo, you'd want to add these to the constructor
    @Autowired
    private DocumentFieldsService documentFieldsService;
    @Autowired
    private AciServiceRetriever aciServiceRetriever;
    @Autowired
    private HavenSearchAciParameterHandler parameterHandler;
    private final Processor<GetContentResponseData> getContentResponseProcessor;

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
    @PreAuthorize(FindRole.HAS_RATING_ROLE)
    @ResponseBody
    public Boolean editDocument(
            @RequestParam("reference") final String reference,
            @RequestParam("database") final String database,
            @RequestParam("field") final String field,
            @RequestParam(value = "value", defaultValue = "") final String value,
            @Value("${content.index.host}") final String indexHost,
            @Value("${content.index.port}") final int indexPort,
            @Value("${conversation.index.DRESYNC}") final boolean DRESYNC
    ) throws IOException, AciHttpException {
        // Check that the field is editable
        final Set<String> idolFields = documentFieldsService.getEditableIdolFields(field);

        if (idolFields.isEmpty()) {
            throw new IllegalArgumentException("Specified field is not editable");
        }

        if (field.contains("\n") || value.contains("\n")) {
            throw new IllegalArgumentException("Illegal characters in field name / value");
        }

        // We need to DREREPLACE into the target engine
        final StringBuilder idx = new StringBuilder("#DREDOCREF " + AciURLCodec.getInstance().encode(reference) + '\n');
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
        final IndexingServiceImpl indexingService = new IndexingServiceImpl(new ServerDetails(indexHost, indexPort), client);
        indexingService.executeCommand(command);

        if (DRESYNC) {
            indexingService.executeCommand(new DreSyncCommand());
        }

        return true;
    }


    @RequestMapping(value = "edit-document-rating", method = RequestMethod.POST)
    @PreAuthorize(FindRole.HAS_RATING_ROLE)
    @ResponseBody
    public Boolean editDocumentRating(
            @RequestParam("reference") final String reference,
            @RequestParam("database") final String database,
            @RequestParam("field") final String field,
            @RequestParam(value = "value", required = false) final Integer value,
            @Value("${content.index.host}") final String indexHost,
            @Value("${content.index.port}") final int indexPort,
            @Value("${conversation.index.DRESYNC}") final boolean DRESYNC,
            Principal activeUser
    ) throws IOException, AciHttpException {
        // Check that the field is editable
        final Set<String> idolFields = documentFieldsService.getEditableIdolFields(field);

        if (idolFields.isEmpty()) {
            throw new IllegalArgumentException("Specified field is not editable");
        }

        if (field.contains("\n")) {
            throw new IllegalArgumentException("Illegal characters in field name");
        }

        if (idolFields.size() != 1) {
            throw new IllegalArgumentException("Rating fields should only have one field");
        }

        final String username = activeUser.getName().toUpperCase(Locale.US);
        final String RATING_FIELD = idolFields.iterator().next();
        final String RATING_BY_USER_PREFIX = RATING_FIELD + "_USER_";
        // do a getcontent

        final AciParameters params = new AciParameters(QueryActions.GetContent.name());
        params.add(GetContentParams.Print.name(), "Fields");
        params.add(GetContentParams.PrintFields.name(), new PrintFields(RATING_FIELD, RATING_BY_USER_PREFIX + "*"));
        parameterHandler.addGetContentOutputParameters(params, database, reference, null);

        final GetContentResponseData resp = aciServiceRetriever.getAciService(QueryRequest.QueryType.RAW).executeAction(params, getContentResponseProcessor);

        if (resp.getHits().size() != 1) {
            throw new IllegalArgumentException("There should be exactly one document with the provided reference");
        }

        final Hit hit = resp.getHits().get(0);

        final DocContent documentContent = hit.getContent();

        final HashMap<String, Integer> currentRatings = new HashMap<>();

        if (documentContent != null && CollectionUtils.isNotEmpty(documentContent.getContent())) {
            final NodeList fields = ((Node) documentContent.getContent().get(0)).getChildNodes();

            for (int i = 0; i < fields.getLength(); i++) {
                final Node fieldNode = fields.item(i);

                // Assume the field names are case insensitive
                final String fieldName = fieldNode.getLocalName();
                if (fieldName.startsWith(RATING_BY_USER_PREFIX)) {

                    if (fieldNode.getFirstChild() != null) {
                        try {
                            final int rating = Integer.parseInt(fieldNode.getFirstChild().getNodeValue());
                            final String rawUsername = fieldName.substring(RATING_BY_USER_PREFIX.length());
                            currentRatings.put(URLDecoder.decode(rawUsername, "UTF-8"), rating);
                        }
                        catch(NumberFormatException e) {
                            // ignore the rating, it's not a number
                        }
                    }
                }
            }
        }

        final boolean clearPreviousUserRating = value == null;
        if(clearPreviousUserRating) {
            // The user is clearing their previous rating
            if (currentRatings.remove(username) == null) {
                // we don't need to do anything, the user has never rated this document before
                return true;
            }
        }
        else {
            // The user is setting a rating
            final Integer oldRating = currentRatings.put(username, value);

            if (oldRating != null && oldRating.intValue() == value) {
                // we don't need to do anything, the user is setting it to the same rating
                return true;
            }
        }

        final OptionalDouble avg = currentRatings.entrySet().stream().mapToDouble(Map.Entry::getValue).average();


        // We need to DREREPLACE into the target engine
        final StringBuilder idx = new StringBuilder("#DREDOCREF " + AciURLCodec.getInstance().encode(reference) + '\n');

        final String userIdolField = RATING_BY_USER_PREFIX + URLEncoder.encode(username, "UTF-8");

        if (clearPreviousUserRating) {
            idx.append("#DREDELETEFIELD ").append(userIdolField).append("\n");
        }
        else {
            idx.append("#DREFIELDNAME ").append(userIdolField).append("\n");
            idx.append("#DREFIELDVALUE ").append(value).append("\n");
        }

        if (!avg.isPresent()) {
            idx.append("#DREDELETEFIELD ").append(RATING_FIELD).append("\n");
        }
        else {
            idx.append("#DREFIELDNAME ").append(RATING_FIELD).append("\n");
            idx.append("#DREFIELDVALUE ").append(avg.getAsDouble()).append("\n");
        }

        idx.append("#DREENDDATANOOP\n\n");

        final DreReplaceCommand command = new DreReplaceCommand();
        command.setMultipleValues(false);
        command.setInsertValue(false);
        command.setPostData(idx.toString());
        command.setDatabaseMatch(database);
        final HttpClient client = new HttpClientFactory().createInstance();
        final IndexingServiceImpl indexingService = new IndexingServiceImpl(new ServerDetails(indexHost, indexPort), client);
        indexingService.executeCommand(command);

        if (DRESYNC) {
            indexingService.executeCommand(new DreSyncCommand());
        }

        return true;
    }
}
