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

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.ExportServiceFactory;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.idol.api.CsvExportApi;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IdolCsvExportController extends IdolExportController implements CsvExportApi {
    public IdolCsvExportController(final RequestMapper<IdolQueryRequest> requestMapper,
                                   final ControllerUtils controllerUtils,
                                   final ExportServiceFactory<IdolQueryRequest, AciErrorException> exportServiceFactory,
                                   final IdolDocumentsService documentsService,
                                   final ConfigFileService<IdolFindConfig> configService) {
        super(requestMapper, controllerUtils, exportServiceFactory, documentsService, configService);
    }

    @Override
    protected ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    @Override
    public ResponseEntity<Resource> exportCsv(
            final String queryRequest,
            final List<String> selectedFieldIds
    ) {
        return super.exportQueryResults(queryRequest, selectedFieldIds);
    }
}
