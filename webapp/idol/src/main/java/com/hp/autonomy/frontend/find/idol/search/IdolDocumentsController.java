/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.search.DocumentsController;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
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
import com.hp.autonomy.types.idol.responses.Profile;
import com.hp.autonomy.types.idol.responses.Profiles;
import com.hp.autonomy.types.idol.responses.Term;
import com.hp.autonomy.types.requests.Documents;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(DocumentsController.SEARCH_PATH)
class IdolDocumentsController extends DocumentsController<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, String, IdolQueryRestrictions, IdolGetContentRequestIndex, IdolSearchResult, AciErrorException> {

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

        this.documentSummaryMaxLength = Optional.ofNullable(configService.getConfigResponse())
                .map(ConfigResponse::getConfig)
                .map(IdolFindConfig::getDocumentSummaryMaxLength)
                .orElse(null);

        this.userService = userService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @RequestMapping(value = "recommend-documents", method = RequestMethod.GET)
    @ResponseBody
    public Documents<IdolSearchResult> recommendDocuments(
            @RequestParam(value = "maxResultsPerProfile", defaultValue = "2") final int maxResultsPerProfile,
            @RequestParam(value = "maxTerms", defaultValue = "30") final int maxTerms,
            @RequestParam(value = "maxProfiles", defaultValue = "3") final int maxProfiles,
            @RequestParam(SUMMARY_PARAM) final String summary,
            @RequestParam(value = INDEXES_PARAM, required = false) final List<String> databases,
            @RequestParam(value = FIELD_TEXT_PARAM, defaultValue = "") final String fieldText,
            @RequestParam(value = MIN_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime minDate,
            @RequestParam(value = MAX_DATE_PARAM, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final ZonedDateTime maxDate,
            @RequestParam(value = HIGHLIGHT_PARAM, defaultValue = "true") final boolean highlight,
            @RequestParam(value = MIN_SCORE_PARAM, defaultValue = "0") final int minScore
    ) throws AciErrorException {
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

        return new Documents<>(results, results.size(), null, null, null, null);
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
}
