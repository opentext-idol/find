/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;

import java.util.Set;

class GetSuggestionsAciExecutor<T> {
    private final AciService aciService;
    private final Processor<T> processor;

    GetSuggestionsAciExecutor(final AciService aciService, final Processor<T> processor) {
        this.aciService = aciService;
        this.processor = processor;
    }

    public T executeAction(final Set<AciParameter> parameters) throws GetSuggestionsFailedException {
        try {
            return aciService.executeAction(parameters, processor);
        } catch (final AciErrorException e) {
            throw new GetSuggestionsFailedException(e);
        }
    }
}
