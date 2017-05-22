/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization.templates;

import com.hp.autonomy.frontend.find.core.customization.CustomizationCachingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class TemplatesController {
    public static final String TEMPLATES_PATH = "/customization/result-templates";

    private final TemplatesService service;
    private final CustomizationCachingStrategy cachingStrategy;

    @Autowired
    public TemplatesController(final TemplatesService service, final CustomizationCachingStrategy cachingStrategy) {
        this.service = service;
        this.cachingStrategy = cachingStrategy;
    }

    @RequestMapping(value = TEMPLATES_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> getTemplates() {
        final Templates templates = service.getTemplates();
        return cachingStrategy.addCacheHeaders(templates.getTemplates(), templates.getLastModified());
    }
}
