/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciParameter;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GetSuggestionsAciExecutorImpl implements GetSuggestionsAciExecutor {
    @Override
    public <T> T executeAction(final AciService aciService, final Processor<T> processor, final Set<AciParameter> parameters) throws GetSuggestionsFailedException {
        try {
            return aciService.executeAction(parameters, processor);
        } catch (final AciErrorException e) {
            throw new GetSuggestionsFailedException(e);
        }
    }
}
