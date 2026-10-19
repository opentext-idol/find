/*
 * Copyright 2015-2017 Open Text.
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

package com.hp.autonomy.frontend.find.core.customization.templates;

import com.hp.autonomy.frontend.find.core.api.TemplatesApi;
import com.hp.autonomy.frontend.find.core.customization.CustomizationCachingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TemplatesController implements TemplatesApi {
    private final TemplatesService service;
    private final CustomizationCachingStrategy cachingStrategy;

    @Autowired
    public TemplatesController(final TemplatesService service, final CustomizationCachingStrategy cachingStrategy) {
        this.service = service;
        this.cachingStrategy = cachingStrategy;
    }

    @Override
    public ResponseEntity<Map<String, String>> getResultTemplates() {
        final Templates templates = service.getTemplates();
        return cachingStrategy.addCacheHeaders(templates.getTemplates(), templates.getLastModified());
    }
}
