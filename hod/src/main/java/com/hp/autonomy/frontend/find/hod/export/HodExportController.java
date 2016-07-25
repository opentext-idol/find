/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class HodExportController extends ExportController<ResourceIdentifier, HodErrorException> {
    @Autowired
    public HodExportController(final ExportService<ResourceIdentifier, HodErrorException> exportService,
                               final RequestMapper<ResourceIdentifier> requestMapper,
                               final ControllerUtils controllerUtils) {
        super(exportService, requestMapper, controllerUtils);
    }
}
