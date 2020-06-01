/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.*;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@IdolService
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
public class SavedSnapshotService extends AbstractSavedSearchService<SavedSnapshot, SavedSnapshot.Builder> {
    private static final Integer STATE_TOKEN_MAX_RESULTS = Integer.MAX_VALUE;

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

        return documentsService.getStateTokenAndResultCount(restrictions, STATE_TOKEN_MAX_RESULTS, promotions);
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

}
