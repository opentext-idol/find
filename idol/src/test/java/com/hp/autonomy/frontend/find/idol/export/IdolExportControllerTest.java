/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportControllerTest;

public class IdolExportControllerTest extends ExportControllerTest<String, AciErrorException> {
    @Override
    protected ExportController<String, AciErrorException> constructController() {
        return new IdolExportController(exportService, requestMapper, controllerUtils);
    }
}
