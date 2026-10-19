/*
 * Copyright 2015 Open Text.
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

package com.hp.autonomy.frontend.find.core.typeahead;

import com.hp.autonomy.frontend.find.core.api.TypeaheadApi;
import com.hp.autonomy.frontend.find.core.api.model.SuggestionsResponse;
import com.hp.autonomy.searchcomponents.core.typeahead.TypeAheadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TypeAheadController<E extends RuntimeException> implements TypeaheadApi {
    private final TypeAheadService<E> typeAheadService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public TypeAheadController(final TypeAheadService<E> typeAheadService) {
        this.typeAheadService = typeAheadService;
    }

    @Override
    public ResponseEntity<SuggestionsResponse> getSuggestions(final String text) {
        return ResponseEntity.ok(new SuggestionsResponse(typeAheadService.getSuggestions(text)));
    }
}
