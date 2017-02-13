/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint;

import java.io.IOException;
import java.io.InputStream;

public interface TemplateSource {
    InputStream getInputStream() throws IOException;

    TemplateSource DEFAULT = () -> TemplateSource.class.getResourceAsStream("/templates/template.pptx");
}
