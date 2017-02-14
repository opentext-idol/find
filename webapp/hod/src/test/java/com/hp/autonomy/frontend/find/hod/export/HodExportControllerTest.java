/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.ExportController;
import com.hp.autonomy.frontend.find.core.export.ExportControllerTest;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;

public class HodExportControllerTest extends ExportControllerTest<HodQueryRequest, HodErrorException> {
    @Override
    protected ExportController<HodQueryRequest, HodErrorException> constructController() {
        return new HodExportController(exportService, requestMapper, controllerUtils, objectMapper);
    }
}
