/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.customization.StyleSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/customization")
public class StyleController {
    private final StyleSheetService service;

    @Autowired
    public StyleController(final StyleSheetService service) {this.service = service;}

    @RequestMapping("/style/{fileName}")
    @ResponseBody
    public String getCss(@PathVariable final String fileName) {
        return service.getCss(fileName)
            // Should never happen
            .orElseThrow(() -> new IllegalStateException("Unknown file requested: " + fileName));
    }
}
