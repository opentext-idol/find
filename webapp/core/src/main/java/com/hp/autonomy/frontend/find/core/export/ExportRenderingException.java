/*
 * Copyright 2026 Open Text.
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

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;

/**
 * Unchecked {@link TemplateLoadException} for throwing in controller methods.
 */
public class ExportRenderingException extends RuntimeException {

    public ExportRenderingException(final TemplateLoadException cause) {
        super(cause.getMessage(), cause);
    }

}
