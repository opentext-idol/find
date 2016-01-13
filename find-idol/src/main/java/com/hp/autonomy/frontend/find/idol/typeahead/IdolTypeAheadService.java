/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class IdolTypeAheadService implements TypeAheadService {
    private final ConfigService<IdolFindConfig> configService;
    private final TermExpandTypeAheadService termExpandService;
    private final QmsTypeAheadService qmsService;

    @Autowired
    public IdolTypeAheadService(
            final ConfigService<IdolFindConfig> configService,
            final TermExpandTypeAheadService termExpandService,
            final QmsTypeAheadService qmsService
    ) {
        this.configService = configService;
        this.termExpandService = termExpandService;
        this.qmsService = qmsService;
    }

    @Override
    public List<String> getSuggestions(final String text) throws GetSuggestionsFailedException{
        if (configService.getConfig().getQueryManipulation().isEnabled()) {
            return qmsService.getSuggestions(text);
        } else {
            return termExpandService.getSuggestions(text);
        }
    }
}
