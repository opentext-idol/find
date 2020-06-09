/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.typeahead;

import com.hp.autonomy.searchcomponents.core.typeahead.TypeAheadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TypeAheadController<E extends Exception> {
    public static final String URL = "/api/public/typeahead";
    static final String TEXT_PARAMETER = "text";

    private final TypeAheadService<E> typeAheadService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public TypeAheadController(final TypeAheadService<E> typeAheadService) {
        this.typeAheadService = typeAheadService;
    }

    @RequestMapping(URL)
    public List<String> getSuggestions(@RequestParam(TEXT_PARAMETER) final String text) throws E {
        return typeAheadService.getSuggestions(text);
    }
}
