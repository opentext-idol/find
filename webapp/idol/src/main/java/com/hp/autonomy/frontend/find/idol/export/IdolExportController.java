/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
class IdolExportController extends ExportController<IdolQueryRequest, AciErrorException> {

    @Autowired
    public IdolExportController(final ExportService<IdolQueryRequest, AciErrorException> exportService,
                                final RequestMapper<IdolQueryRequest> requestMapper,
                                final ControllerUtils controllerUtils,
                                final ObjectMapper objectMapper) {
        super(exportService, requestMapper, controllerUtils, objectMapper);
    }
}
