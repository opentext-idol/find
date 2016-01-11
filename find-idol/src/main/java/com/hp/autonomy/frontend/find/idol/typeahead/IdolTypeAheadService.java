/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

// TODO: Implement IDOL TypeAheadService
@Service
public class IdolTypeAheadService implements TypeAheadService {
    @Override
    public List<String> getSuggestions(final String text) {
        return Collections.emptyList();
    }
}
