/*
 * Copyright 2020 Open Text.
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

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.*;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Service
@IdolService
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class SavedSnapshotService extends AbstractSavedSearchService<SavedSnapshot, SavedSnapshot.Builder> {
    // maximum allowed value for config MaxValue
    private static final Integer STATE_TOKEN_MAX_RESULTS = 1_000_000;

    private final IdolDocumentsService documentsService;
    private final FieldTextParser fieldTextParser;
    private final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Autowired
    public SavedSnapshotService(final SavedSearchRepository<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotRepository,
                                final SharedToUserRepository sharedToUserRepository,
                                final SharedToEveryoneRepository sharedToEveryoneRepository,
                                final AuditorAware<UserEntity> userEntityAuditorAware,
                                final TagNameFactory tagNameFactory,
                                final IdolDocumentsService documentsService,
                                final FieldTextParser fieldTextParser,
                                final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory) {
        super(savedSnapshotRepository, sharedToUserRepository, sharedToEveryoneRepository, userEntityAuditorAware, tagNameFactory, SavedSnapshot.class);
        this.documentsService = documentsService;
        this.fieldTextParser = fieldTextParser;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
    }

    private StateTokenAndResultCount getStateTokenAndResultCount(final SavedSearch<?, ?> query, final boolean promotions) throws AciErrorException {
        final List<String> indexes;

        if(query.getIndexes() == null) {
            indexes = null;
        } else {
            indexes = new ArrayList<>();

            indexes.addAll(query.getIndexes().stream().map(EmbeddableIndex::getName).collect(Collectors.toList()));
        }

        final IdolQueryRestrictions restrictions = queryRestrictionsBuilderFactory.getObject()
            .databases(indexes)
            .queryText(query.toQueryText())
            .fieldText(fieldTextParser.toFieldText(query, !promotions))
            .maxDate(query.getMaxDate())
            .minDate(query.getMinDate())
            .minScore(query.getMinScore())
            .build();

        return documentsService.getStateTokenAndResultCount(
            restrictions, STATE_TOKEN_MAX_RESULTS, QueryRequest.QueryType.MODIFIED, promotions);
    }

    @Override
    public SavedSnapshot build(final SavedSearch<?, ?> search) throws AciErrorException {
        final List<StateTokenAndResultCount> stateTokensList = Arrays.asList(
            getStateTokenAndResultCount(search, false),
            getStateTokenAndResultCount(search, true)
        );

        final Set<TypedStateToken> stateTokens = new HashSet<>();

        for(final StateTokenAndResultCount listToken : stateTokensList) {
            final TypedStateToken token = listToken.getTypedStateToken();
            stateTokens.add(token);
        }

        // use result count from the query without promotions
        final long resultCount = stateTokensList.get(0).getResultCount();

        return new SavedSnapshot.Builder(search)
            .setStateTokens(stateTokens)
            .setResultCount(resultCount)
            .build();
    }

    /**
     * Retrieve a state token with type QUERY for either a snapshot or query.  Exactly one of
     * `snapshotId` and `query` is required.
     *
     * @param savedSnapshotService
     * @param snapshotId
     * @param query
     * @return State token
     */
    public static TypedStateToken toSnapshotToken(
        final SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService,
        final Long snapshotId,
        final SavedQuery query
    ) {
        if (snapshotId == null && query == null) {
            throw new IllegalArgumentException("Snapshot ID or saved query required");
        }
        if (snapshotId != null && query != null) {
            throw new IllegalArgumentException("Only one of snapshot ID and saved query allowed");
        }

        final SavedSnapshot snapshot;
        if (snapshotId != null) {
            snapshot = savedSnapshotService.getDashboardSearch(snapshotId);
            if (snapshot == null) {
                throw new IllegalArgumentException("No Saved Snapshot found with ID " + snapshotId);
            }
        } else {
            snapshot = savedSnapshotService.build(query);
        }

        return snapshot.getStateTokens().stream()
            .filter(x -> x.getType().equals(TypedStateToken.StateTokenType.QUERY))
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Saved Snapshot has no associated state token"));
    }

}
