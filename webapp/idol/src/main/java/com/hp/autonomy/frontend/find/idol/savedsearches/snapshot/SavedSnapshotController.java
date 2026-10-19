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

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot.Builder;
import com.hp.autonomy.frontend.find.idol.api.SavedSnapshotApi;
import com.hp.autonomy.frontend.find.idol.api.model.SavedSnapshotsResponse;
import com.hp.autonomy.frontend.find.idol.dashboards.DashboardConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.DatasourceDependentWidget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchDatasource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
class SavedSnapshotController implements SavedSnapshotApi {
    private final SavedSearchService<SavedSnapshot, Builder> service;
    private final ConfigService<DashboardConfig> dashboardConfigService;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public SavedSnapshotController(final SavedSearchService<SavedSnapshot, Builder> service,
                                   final ConfigService<DashboardConfig> dashboardConfigService) {
        this.service = service;
        this.dashboardConfigService = dashboardConfigService;
    }

    private Collection<Long> getValidIds() {
        return dashboardConfigService.getConfig().getDashboards().stream()
                .flatMap(dashboard -> dashboard.getWidgets().stream()
                        .filter(widget -> widget instanceof DatasourceDependentWidget)
                        .map(widget -> (DatasourceDependentWidget) widget)
                        .filter(widget -> widget.getDatasource() instanceof SavedSearchDatasource)
                        .map(widget -> (SavedSearchDatasource) widget.getDatasource())
                        .filter(datasource -> datasource.getConfig().getType() == SavedSearchType.SNAPSHOT)
                        .map(datasource -> datasource.getConfig().getId()))
                .collect(Collectors.toSet());
    }

    @Override
    public ResponseEntity<SavedSnapshotsResponse> getSavedSnapshots() {
        return ResponseEntity.ok(new SavedSnapshotsResponse(service.getOwned()));
    }

    @Override
    public ResponseEntity<SavedSnapshotsResponse> getSharedSavedSnapshots() {
        return ResponseEntity.ok(new SavedSnapshotsResponse(service.getShared()));
    }

    @Override
    public ResponseEntity<SavedSnapshot> createSavedSnapshot(final SavedQuery query) {
        return ResponseEntity.ok(service.create(service.build(query)));
    }

    @Override
    public ResponseEntity<SavedSnapshot> updateSavedSnapshot(
            final Long id,
            final SavedSnapshot snapshot
    ) {
        // It is only possible to update a snapshot's title
        return ResponseEntity.ok(service.update(
                new Builder()
                        .setId(id)
                        .setTitle(snapshot.getTitle())
                        .build()
        ));
    }

    @Override
    public ResponseEntity<SavedSnapshot> updateSharedSavedSnapshot(
            final Long id,
            final SavedSnapshot snapshot
    ) {
        // It is only possible to update a snapshot's title
        return ResponseEntity.ok(service.update(
                new Builder()
                        .setId(id)
                        .setTitle(snapshot.getTitle())
                        .build()
        ));
    }

    @Override
    public ResponseEntity<Void> deleteSavedSnapshot(final Long id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<SavedSnapshot> getSavedSnapshot(final Long id) {
        if(getValidIds().contains(id)) {
            return ResponseEntity.ok(Optional.ofNullable(service.getDashboardSearch(id))
                    .orElseThrow(() -> new IllegalArgumentException("Configured ID " + id + " does not match any known Saved Snapshot")));
        } else {
            throw new IllegalArgumentException("Saved Snapshot ID " + id + " is not in the dashboards configuration file");
        }
    }
}
