/*
 * Copyright 2015 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.frontend.find.idol.api.SearchApi;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.*;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import com.opentext.idol.types.responses.Profile;
import com.opentext.idol.types.responses.Profiles;
import com.opentext.idol.types.responses.Term;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
class IdolDocumentsController extends DocumentsController<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, String, IdolQueryRestrictions, IdolGetContentRequestIndex, IdolSearchResult, AciErrorException> implements SearchApi {

    private final ConfigService<IdolFindConfig> configService;
    private final Integer documentSummaryMaxLength;
    private final UserService userService;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    public IdolDocumentsController(final IdolDocumentsService documentsService,
                                   final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                                   final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory,
                                   final ObjectFactory<IdolSuggestRequestBuilder> suggestRequestBuilderFactory,
                                   final ObjectFactory<IdolGetContentRequestBuilder> getContentRequestBuilderFactory,
                                   final ObjectFactory<IdolGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory,
                                   final ConfigFileService<IdolFindConfig> configService,
                                   final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever, UserService userService) {
        super(documentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory);
        this.configService = configService;

        this.documentSummaryMaxLength = Optional.ofNullable(configService.getConfigResponse())
                .map(ConfigResponse::getConfig)
                .map(IdolFindConfig::getDocumentSummaryMaxLength)
                .orElse(null);

        this.userService = userService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ResponseEntity<Documents<IdolSearchResult>> recommendDocuments(
            final String summary,
            final Integer maxResultsPerProfile,
            final Integer maxTerms,
            final Integer maxProfiles,
            final List<String> databases,
            final String fieldText,
            final ZonedDateTime minDate,
            final ZonedDateTime maxDate,
            final Boolean highlight,
            final Integer minScore
    ) {
        // Use top M terms, from N profiles, taking top X results.

        final Profiles profiles = userService.profileRead(authenticationInformationRetriever.getPrincipal().getName());

        final List<IdolSearchResult> results = new ArrayList<>();

        final Set<String> uniqueRefs = new HashSet<>();

        int profileIdx = 0;
        for(final Profile profile : profiles.getProfile()) {
            if (++profileIdx > maxProfiles || profile.getTerm().isEmpty()) {
                break;
            }

            final List<Term> allTerms = profile.getTerm();
            allTerms.sort(Comparator.comparingInt(Term::getWeight).reversed());

            final List<Term> usableTerms = allTerms.subList(0, Math.min(allTerms.size(), maxTerms));

            final String terms = usableTerms.stream().map(
                term -> term.getValue() + "~[" + term.getWeight() + "]"
            ).collect(Collectors.joining(" "));

            final IdolQueryRestrictions queryRestrictions = queryRestrictionsBuilderFactory.getObject()
                    .queryText(terms)
                    .fieldText(fieldText)
                    .databases(ListUtils.emptyIfNull(databases))
                    .minDate(minDate)
                    .maxDate(maxDate)
                    .minScore(minScore)
                    .build();

            final IdolQueryRequest queryRequest = queryRequestBuilderFactory.getObject()
                    .queryRestrictions(queryRestrictions)
                    .start(1)
                    .maxResults(maxResultsPerProfile)
                    .summaryCharacters(getMaxSummaryCharacters())
                    .highlight(highlight)
                    .autoCorrect(false)
                    .summary(summary)
                    .sort("relevance")
                    .queryType(QueryRequest.QueryType.RAW)
                    .intentBasedRanking(false)
                    .referenceField(getReferenceField())
                    .build();

            final Documents<IdolSearchResult> profileDocs = documentsService.queryTextIndex(queryRequest);

            for(final IdolSearchResult result : profileDocs.getDocuments()) {
                final String reference = result.getReference();
                if (!uniqueRefs.contains(reference)) {
                    results.add(result);
                    uniqueRefs.add(reference);
                }
            }
        }

        return ResponseEntity.ok(new Documents<>(results, results.size(), null, null, null, null));
    }

    @Override
    public ResponseEntity<Documents<IdolSearchResult>> queryTextIndex(
            final String text,
            final Integer maxResults,
            final String summary,
            final Integer start,
            final List<String> indexes,
            final String fieldText,
            final ZonedDateTime minDate,
            final ZonedDateTime maxDate,
            final Boolean highlight,
            final Integer minScore,
            final String sort,
            final Boolean autoCorrect,
            final Boolean intentBasedRanking,
            final QueryRequest.QueryType queryType
    ) {
        return ResponseEntity.ok(super.query(text, start, maxResults, summary, indexes, fieldText,
                sort, minDate, maxDate, highlight, minScore, autoCorrect, intentBasedRanking,
                queryType));
    }

    @Override
    public ResponseEntity<Documents<IdolSearchResult>> findSimilarDocuments(
            final String reference,
            final Integer maxResults,
            final String summary,
            final Integer start,
            final List<String> indexes,
            final String fieldText,
            final ZonedDateTime minDate,
            final ZonedDateTime maxDate,
            final Boolean highlight,
            final Integer minScore,
            final String sort
    ) {
        return ResponseEntity.ok(super.findSimilar(reference, start, maxResults, summary, indexes,
                fieldText, sort, minDate, maxDate, highlight, minScore));
    }

    @Override
    public ResponseEntity<IdolSearchResult> getDocumentContent(
            final String reference,
            final String database
    ) {
        return ResponseEntity.ok(super.fetchDocumentContent(reference, database));
    }

    @Override
    protected <T> T throwException(final String message) throws AciErrorException {
        throw new AciErrorException(message);
    }

    @Override
    protected void addParams(final GetContentRequestBuilder<IdolGetContentRequest, IdolGetContentRequestIndex, ?> request) {
        ((IdolGetContentRequestBuilder) request)
                .print(PrintParam.All);
    }

    @Override
    protected Integer getMaxSummaryCharacters() {
        return this.documentSummaryMaxLength;
    }

    protected String getReferenceField() {
        return configService.getConfig().getReferenceField();
    }
}
