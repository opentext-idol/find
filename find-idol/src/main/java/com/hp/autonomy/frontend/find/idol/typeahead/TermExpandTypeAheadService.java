/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.typeahead.GetSuggestionsFailedException;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadConstants;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadService;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.TermExpandResponseData;
import com.hp.autonomy.types.requests.idol.actions.term.TermActions;
import com.hp.autonomy.types.requests.idol.actions.term.params.ExpandTypeParam;
import com.hp.autonomy.types.requests.idol.actions.term.params.ExpansionParam;
import com.hp.autonomy.types.requests.idol.actions.term.params.TermExpandParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class TermExpandTypeAheadService implements TypeAheadService {
    private final GetSuggestionsAciExecutor<TermExpandResponseData> executor;

    @Autowired
    public TermExpandTypeAheadService(
            final AciService contentAciService,
            final AciResponseJaxbProcessorFactory processorFactory
    ) {
        this.executor = new GetSuggestionsAciExecutor<>(
                contentAciService,
                processorFactory.createAciResponseProcessor(TermExpandResponseData.class)
        );
    }

    @Override
    public List<String> getSuggestions(final String text) throws GetSuggestionsFailedException {
        final AciParameters parameters = new AciParameters(TermActions.TermExpand.name());
        parameters.put(TermExpandParams.Expansion.name(), ExpansionParam.Wild);
        parameters.put(TermExpandParams.Stemming.name(), false);
        parameters.put(TermExpandParams.MaxTerms.name(), TypeAheadConstants.MAX_RESULTS);
        parameters.put(TermExpandParams.Type.name(), ExpandTypeParam.DocOccs);
        parameters.put(TermExpandParams.Text.name(), text);

        final TermExpandResponseData response = executor.executeAction(parameters);
        final List<String> output = new LinkedList<>();

        for (final TermExpandResponseData.Term term : response.getTerm()) {
            output.add(term.getValue().toLowerCase());
        }

        return output;
    }
}
