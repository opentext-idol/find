/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.typeahead;

import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadService;
import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.frontend.find.hod.beanconfiguration.HodConfiguration;
import com.hp.autonomy.hod.client.api.analysis.autocomplete.AutocompleteService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HodTypeAheadService implements TypeAheadService {
    private final AutocompleteService autocompleteService;

    @Autowired
    public HodTypeAheadService(final AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    @Override
    @Cacheable(cacheNames = FindCacheNames.TYPE_AHEAD, cacheResolver = HodConfiguration.SIMPLE_CACHE_RESOLVER_NAME)
    public List<String> getSuggestions(final String text) throws GetSuggestionsFailedException {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        } else {
            try {
                return autocompleteService.getSuggestions(text);
            } catch (final HodErrorException e) {
                throw new GetSuggestionsFailedException(e);
            }
        }
    }
}
