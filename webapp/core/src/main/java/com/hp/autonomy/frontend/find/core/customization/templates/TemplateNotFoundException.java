/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization.templates;

import lombok.Getter;

@Getter
class TemplateNotFoundException extends Exception {
    private static final long serialVersionUID = 2670666376249037070L;

    private final String fileName;
    private final String templateDirectory;

    TemplateNotFoundException(final String fileName, final String templateDirectory) {
        super("Template \"" + fileName + "\" not found in directory \"" + templateDirectory + '"');
        this.templateDirectory = templateDirectory;
        this.fileName = fileName;
    }
}
