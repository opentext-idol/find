/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
