/*
 * Copyright 2018 Open Text.
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

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.api.model.SavedQueriesResponse;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.FieldTextParser;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery.Builder;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryController;
import com.hp.autonomy.frontend.find.idol.api.SavedQueryApi;
import com.hp.autonomy.frontend.find.idol.dashboards.DashboardConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.DatasourceDependentWidget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchDatasource;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.*;
import com.hp.autonomy.types.requests.idol.actions.query.params.PrintParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
class IdolSavedQueryController extends SavedQueryController<IdolQueryRequest, String, IdolQueryRestrictions, IdolSearchResult, AciErrorException> implements SavedQueryApi {
    private final ConfigService<DashboardConfig> dashboardConfigService;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    IdolSavedQueryController(final SavedSearchService<SavedQuery, Builder> service,
                             final IdolDocumentsService documentsService,
                             final FieldTextParser fieldTextParser,
                             final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory,
                             final ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory,
                             final ConfigService<DashboardConfig> dashboardConfigService) {
        super(service, documentsService, fieldTextParser, queryRestrictionsBuilderFactory, queryRequestBuilderFactory);
        this.dashboardConfigService = dashboardConfigService;
    }

    @Override
    protected String convertEmbeddableIndex(final EmbeddableIndex embeddableIndex) {
        return embeddableIndex.getName();
    }

    @Override
    protected void addParams(final QueryRequestBuilder<IdolQueryRequest, IdolQueryRestrictions, ?> queryRequestBuilder) {
        queryRequestBuilder.print(PrintParam.NoResults.name());
    }

    @Override
    public ResponseEntity<SavedQuery> getSavedQuery(final Long id) {
        if(getValidIds().contains(id)) {
            return ResponseEntity.ok(Optional.ofNullable(service.getDashboardSearch(id))
                .orElseThrow(() -> new IllegalArgumentException("Configured ID " + id + " does not match any known Saved Query")));
        } else {
            throw new IllegalArgumentException("Saved Query ID " + id + " is not in the dashboards configuration file");
        }
    }

    @Override
    public ResponseEntity<SavedQueriesResponse> getSavedQueries(final Boolean shared) {
        return ResponseEntity.ok(super.getAll(shared));
    }

    @Override
    public ResponseEntity<SavedQueriesResponse> getSharedSavedQueries() {
        return ResponseEntity.ok(super.getShared());
    }

    @Override
    public ResponseEntity<SavedQuery> createSavedQuery(final SavedQuery query) {
        return ResponseEntity.ok(super.create(query));
    }

    @Override
    public ResponseEntity<SavedQuery> updateSavedQuery(final Long id, final SavedQuery query) {
        return ResponseEntity.ok(super.update(id, query));
    }

    @Override
    public ResponseEntity<SavedQuery> updateSharedSavedQuery(final Long id, final SavedQuery query) {
        return ResponseEntity.ok(super.updateShared(id, query));
    }

    @Override
    public ResponseEntity<Void> deleteSavedQuery(final Long id) {
        super.delete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Integer> countNewSavedQueryResults(final Long id) {
        return ResponseEntity.ok(super.checkForNewQueryResults(id));
    }

    private Collection<Long> getValidIds() {
        return dashboardConfigService.getConfig().getDashboards().stream()
            .flatMap(dashboard -> dashboard.getWidgets().stream()
                .filter(widget -> widget instanceof DatasourceDependentWidget)
                .map(widget -> (DatasourceDependentWidget)widget)
                .filter(widget -> widget.getDatasource() instanceof SavedSearchDatasource)
                .map(widget -> (SavedSearchDatasource)widget.getDatasource())
                .filter(datasource -> datasource.getConfig().getType() == SavedSearchType.QUERY)
                .map(datasource -> datasource.getConfig().getId()))
            .collect(Collectors.toSet());
    }
}
