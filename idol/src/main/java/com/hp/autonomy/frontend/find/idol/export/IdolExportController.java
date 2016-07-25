/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class IdolExportController extends ExportController<String, AciErrorException> {
    @Autowired
    public IdolExportController(final ExportService<String, AciErrorException> exportService,
                                final RequestMapper<String> requestMapper,
                                final ControllerUtils controllerUtils) {
        super(exportService, requestMapper, controllerUtils);
    }
}
