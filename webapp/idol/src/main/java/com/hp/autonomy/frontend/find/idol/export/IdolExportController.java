/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.core.search.StateTokenAndResultCount;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.OutputStream;
import java.util.Collection;

@Controller
class IdolExportController extends ExportController<IdolQueryRequest, AciErrorException> {

    private final IdolDocumentsService documentsService;
    private final ExportService<IdolQueryRequest, AciErrorException> exportService;

    @Autowired
    public IdolExportController(final RequestMapper<IdolQueryRequest> requestMapper,
                                final ControllerUtils controllerUtils,
                                final IdolDocumentsService documentsService,
                                final ExportService<IdolQueryRequest, AciErrorException> exportService,
                                final ObjectMapper objectMapper,
                                final ConfigService<IdolFindConfig> configService) {
        super(requestMapper, controllerUtils, objectMapper, configService);
        this.documentsService = documentsService;
        this.exportService = exportService;
    }

    @Override
    protected void export(final OutputStream outputStream,
                          final IdolQueryRequest queryRequest,
                          final ExportFormat exportFormat,
                          final Collection<String> selectedFieldNames) throws AciErrorException {
        final StateTokenAndResultCount stateTokenAndResultCount = documentsService.getStateTokenAndResultCount(queryRequest.getQueryRestrictions(), queryRequest.getMaxResults(), false);

        final IdolQueryRequest queryRequestWithStateToken = queryRequest.toBuilder()
                .queryRestrictions(queryRequest.getQueryRestrictions().toBuilder()
                        .stateMatchId(stateTokenAndResultCount.getTypedStateToken().getStateToken())
                        .build())
                .build();

        for (int i = 0; i < stateTokenAndResultCount.getResultCount(); i += PAGINATION_SIZE) {
            final IdolQueryRequest paginatedQueryRequest = queryRequestWithStateToken.toBuilder()
                    .start(i + 1)
                    .maxResults(i + PAGINATION_SIZE)
                    .build();
            exportService.export(outputStream, paginatedQueryRequest, exportFormat, selectedFieldNames);
        }
    }
}
