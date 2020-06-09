/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.customization.CustomizationCachingStrategy;
import com.hp.autonomy.frontend.find.core.customization.style.StyleSheet;
import com.hp.autonomy.frontend.find.core.customization.style.StyleSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/customization/style")
public class StyleController {
    private final StyleSheetService service;
    private final CustomizationCachingStrategy cachingStrategy;

    @Autowired
    public StyleController(final StyleSheetService service, final CustomizationCachingStrategy cachingStrategy) {
        this.service = service;
        this.cachingStrategy = cachingStrategy;
    }

    @RequestMapping("/{fileName}")
    @ResponseBody
    public ResponseEntity<String> getCss(@PathVariable final String fileName) {
        final StyleSheet styleSheet = service.getCss(fileName)
                // Should never happen - browser should not request invalid style sheet
                .orElseThrow(() -> new IllegalStateException("Unknown file requested: " + fileName));

        return cachingStrategy.addCacheHeaders(styleSheet.getStyleSheet(), styleSheet.getLastModified());
    }
}
