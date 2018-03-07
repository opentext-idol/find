/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;

abstract class IdolExportController extends ExportController<IdolQueryRequest, AciErrorException> {

    private final IdolDocumentsService documentsService;

    private final int stateTokenMaxResults;

    protected IdolExportController(final RequestMapper<IdolQueryRequest> requestMapper,
                                   final ControllerUtils controllerUtils,
                                   final ExportServiceFactory<IdolQueryRequest, AciErrorException> exportServiceFactory,
                                   final IdolDocumentsService documentsService,
                                   final ConfigFileService<IdolFindConfig> configService) {
        super(requestMapper, controllerUtils, exportServiceFactory);
        this.documentsService = documentsService;

        this.stateTokenMaxResults = Optional.ofNullable(configService.getConfigResponse())
                .map(ConfigResponse::getConfig)
                .map(IdolFindConfig::getExportStoreStateMaxResults)
                .orElse(Integer.MAX_VALUE);
    }

    @Override
    protected void export(final OutputStream outputStream,
                          final IdolQueryRequest queryRequest,
                          final Collection<String> selectedFieldNames) throws AciErrorException, IOException {
        final int maxResults = Math.min(queryRequest.getMaxResults(), this.stateTokenMaxResults);

        final StateTokenAndResultCount stateTokenAndResultCount = documentsService.getStateTokenAndResultCount(
                queryRequest.getQueryRestrictions(), maxResults, false);

        final IdolQueryRequest queryRequestWithStateToken = queryRequest.toBuilder()
                .queryRestrictions(queryRequest.getQueryRestrictions().toBuilder()
                        .stateMatchId(stateTokenAndResultCount.getTypedStateToken().getStateToken())
                        .build())
                .build();

        final ExportFormat exportFormat = getExportFormat();
        final PlatformDataExportService<IdolQueryRequest, AciErrorException> exportService = exportServiceFactory.getPlatformDataExportService(exportFormat)
                .orElseThrow(() -> new UnsupportedOperationException("Query result export not supported for format " + exportFormat.name()));
        exportService.exportQueryResults(outputStream, queryRequestWithStateToken, exportFormat, selectedFieldNames, Math.min(stateTokenAndResultCount.getResultCount(), this.stateTokenMaxResults));
    }
}
