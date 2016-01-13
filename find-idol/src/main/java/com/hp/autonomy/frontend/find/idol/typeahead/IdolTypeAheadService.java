/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.typeahead;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadConstants;
import com.hp.autonomy.frontend.find.core.typeahead.TypeAheadService;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.frontend.find.idol.configuration.OptionalAciService;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.TermExpandResponseData;
import com.hp.autonomy.types.idol.TypeAheadResponseData;
import com.hp.autonomy.types.requests.idol.actions.term.TermActions;
import com.hp.autonomy.types.requests.idol.actions.term.params.ExpandTypeParam;
import com.hp.autonomy.types.requests.idol.actions.term.params.ExpansionParam;
import com.hp.autonomy.types.requests.idol.actions.term.params.TermExpandParams;
import com.hp.autonomy.types.requests.qms.actions.typeahead.TypeAheadActions;
import com.hp.autonomy.types.requests.qms.actions.typeahead.params.ModeParam;
import com.hp.autonomy.types.requests.qms.actions.typeahead.params.TypeAheadParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class IdolTypeAheadService implements TypeAheadService {
    private final ConfigService<IdolFindConfig> configService;
    private final AciService contentAciService;
    private final OptionalAciService qmsAciService;

    private final Processor<TermExpandResponseData> termExpandProcessor;
    private final Processor<TypeAheadResponseData> typeAheadProcessor;

    @Autowired
    public IdolTypeAheadService(
            final ConfigService<IdolFindConfig> configService,
            final AciService contentAciService,
            final OptionalAciService qmsAciService,
            final AciResponseJaxbProcessorFactory processorFactory
    ) {
        this.configService = configService;
        this.contentAciService = contentAciService;
        this.qmsAciService = qmsAciService;

        termExpandProcessor = processorFactory.createAciResponseProcessor(TermExpandResponseData.class);
        typeAheadProcessor = processorFactory.createAciResponseProcessor(TypeAheadResponseData.class);
    }

    @Override
    public List<String> getSuggestions(final String text) {
        if (qmsAciService.isEnabled()) {
            return getQmsSuggestions(text);
        } else {
            return getContentSuggestions(text);
        }
    }

    private List<String> getQmsSuggestions(final String text) {
        final ModeParam mode = configService.getConfig().getQueryManipulation().getTypeAheadMode();

        final AciParameters parameters = new AciParameters(TypeAheadActions.TypeAhead.name());
        parameters.add(TypeAheadParams.Mode.name(), mode);
        parameters.add(TypeAheadParams.MaxResults.name(), TypeAheadConstants.MAX_RESULTS);
        parameters.add(TypeAheadParams.Text.name(), text);

        final TypeAheadResponseData response = qmsAciService.executeAction(parameters, typeAheadProcessor);
        final List<String> output = new LinkedList<>();

        for (final TypeAheadResponseData.Expansion expansion : response.getExpansion()) {
            final String value = expansion.getValue();

            if (ModeParam.Index.equals(mode)) {
                output.add(value.toLowerCase());
            } else {
                // Do not lower case dictionary suggestions (they are explicitly defined by the user)
                output.add(value);
            }
        }

        return output;
    }

    private List<String> getContentSuggestions(final String text) {
        final AciParameters parameters = new AciParameters(TermActions.TermExpand.name());
        parameters.put(TermExpandParams.Expansion.name(), ExpansionParam.Wild);
        parameters.put(TermExpandParams.Stemming.name(), false);
        parameters.put(TermExpandParams.MaxTerms.name(), TypeAheadConstants.MAX_RESULTS);
        parameters.put(TermExpandParams.Type.name(), ExpandTypeParam.DocOccs);
        parameters.put(TermExpandParams.Text.name(), text);

        final TermExpandResponseData response = contentAciService.executeAction(parameters, termExpandProcessor);
        final List<String> output = new LinkedList<>();

        for (final TermExpandResponseData.Term term : response.getTerm()) {
            output.add(term.getValue().toLowerCase());
        }

        return output;
    }
}
