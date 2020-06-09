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

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

abstract class HodExportController extends ExportController<HodQueryRequest, HodErrorException> {

    private final HodDocumentsService documentsService;

    protected HodExportController(final RequestMapper<HodQueryRequest> requestMapper,
                                  final ControllerUtils controllerUtils,
                                  final ExportServiceFactory<HodQueryRequest, HodErrorException> exportServiceFactory,
                                  final HodDocumentsService documentsService) {
        super(requestMapper, controllerUtils, exportServiceFactory);
        this.documentsService = documentsService;
    }

    @Override
    protected void export(final OutputStream outputStream,
                          final HodQueryRequest queryRequest,
                          final Collection<String> selectedFieldNames) throws HodErrorException, IOException {
        final HodQueryRequest queryRequestForCount = queryRequest.toBuilder()
                .maxResults(1)
                .print(Print.no_results.name())
                .build();
        final Documents<HodSearchResult> searchResult = documentsService.queryTextIndex(queryRequestForCount);
        final int totalResults = Math.min(Math.min(searchResult.getTotalResults(), queryRequest.getMaxResults()), HodDocumentsService.HOD_MAX_RESULTS);

        final ExportFormat exportFormat = getExportFormat();
        final PlatformDataExportService<HodQueryRequest, HodErrorException> exportService = exportServiceFactory.getPlatformDataExportService(exportFormat)
                .orElseThrow(() -> new UnsupportedOperationException("Query result export not supported for format " + exportFormat.name()));
        exportService.exportQueryResults(outputStream, queryRequest, exportFormat, selectedFieldNames, totalResults);
    }
}
