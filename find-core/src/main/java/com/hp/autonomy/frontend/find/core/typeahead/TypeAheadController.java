/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.typeahead;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TypeAheadController {
    public static final String URL = "/api/public/typeahead";
    public static final String TEXT_PARAMETER = "text";

    private final TypeAheadService service;

    @Autowired
    public TypeAheadController(final TypeAheadService service) {
        this.service = service;
    }

    @RequestMapping(URL)
    public List<String> getSuggestions(@RequestParam(TEXT_PARAMETER) final String text) throws GetSuggestionsFailedException {
        return service.getSuggestions(text);
    }
}
