/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.OutputStream;
import java.util.Collection;

@Controller
class HodExportController extends ExportController<HodQueryRequest, HodErrorException> {

    private final HodDocumentsService documentsService;
    private final ExportService<HodQueryRequest, HodErrorException> exportService;

    @Autowired
    public HodExportController(final RequestMapper<HodQueryRequest> requestMapper,
                               final ControllerUtils controllerUtils,
                               final HodDocumentsService documentsService,
                               final ExportService<HodQueryRequest, HodErrorException> exportService,
                               final ObjectMapper objectMapper,
                               final ConfigService<HodFindConfig> configService) {
        super(requestMapper, controllerUtils, objectMapper, configService);
        this.documentsService = documentsService;
        this.exportService = exportService;
    }

    @Override
    protected void export(final OutputStream outputStream,
                          final HodQueryRequest queryRequest,
                          final ExportFormat exportFormat,
                          final Collection<String> selectedFieldNames) throws HodErrorException {
        final HodQueryRequest queryRequestForCount = queryRequest.toBuilder()
                .maxResults(1)
                .print(Print.no_results.name())
                .build();
        final Documents<HodSearchResult> searchResult = documentsService.queryTextIndex(queryRequestForCount);
        final int totalResults = Math.min(Math.min(searchResult.getTotalResults(), queryRequest.getMaxResults()), HodDocumentsService.HOD_MAX_RESULTS);

        for (int i = 0; i < totalResults; i += PAGINATION_SIZE) {
            final HodQueryRequest paginatedQueryRequest = queryRequest.toBuilder()
                    .start(i + 1)
                    .maxResults(Math.min(i + PAGINATION_SIZE, HodDocumentsService.HOD_MAX_RESULTS))
                    .build();
            exportService.export(outputStream, paginatedQueryRequest, exportFormat, selectedFieldNames);
        }
    }
}
