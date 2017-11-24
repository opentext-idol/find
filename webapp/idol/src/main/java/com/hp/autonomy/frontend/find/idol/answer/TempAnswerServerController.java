/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.EntitySearchConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.answer.AskAnswer;
import com.hp.autonomy.types.idol.responses.answer.AskAnswers;
import com.hp.autonomy.types.idol.responses.answer.AskResponsedata;
import com.hp.autonomy.types.requests.idol.actions.answer.params.AskParams;
import com.hp.autonomy.types.requests.idol.actions.answer.params.AskSortParam;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.hp.autonomy.frontend.find.idol.answer.AnswerServerController.MAX_RESULTS_PARAM;
import static com.hp.autonomy.frontend.find.idol.answer.AnswerServerController.TEXT_PARAM;

// This is a temporary controller, long term we'd use AnswerServerController, but the demo network has an older build
//   of answer server at the moment so we need custom parsing.
@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class TempAnswerServerController {

    private final AciService aciService;
    private final HavenSearchAciParameterHandler parameterHandler;
    private final ConfigService<IdolFindConfig> configService;

    private final Processor<AskResponsedata> processor;

    @Autowired
    TempAnswerServerController(final AciService aciService,
                               final HavenSearchAciParameterHandler parameterHandler,
                               final ConfigService<IdolFindConfig> configService,
                               final ProcessorFactory processorFactory) {
        this.aciService = aciService;
        this.parameterHandler = parameterHandler;
        this.configService = configService;
        processor = processorFactory.getResponseDataProcessor(AskResponsedata.class);
    }

    @RequestMapping(value = "ask-demo", method = RequestMethod.GET)
    public List<AskAnswer> ask(
            @RequestParam(TEXT_PARAM) final String text,
            @RequestParam(value = MAX_RESULTS_PARAM, required = false)
            final Integer maxResults
    ) throws XPathExpressionException {
        final ArrayList<AskAnswer> toReturn = new ArrayList<>();

        if (maxResults != null && maxResults == 0) {
            return toReturn;
        }

        final IdolFindConfig config = configService.getConfig();

        final EntitySearchConfig entitySearch = config.getEntitySearch();
        final AnswerServerConfig answerServer = entitySearch.getAnswerServer();
        final AciServerDetails details = answerServer.toAciServerDetails();
        final Collection<String> systemNames = answerServer.getSystemNames();

        // We deliberately do each systemName in sequence since FactBank is faster than PassageExtraction.
        // If the systemNames aren't listed, we'll use null, which means we'll try all configured answer server systems.
        for(String systemName : CollectionUtils.isEmpty(systemNames) ? Collections.<String>singletonList(null) : systemNames) {
            final AciParameters params = new AciParameters("ask");

            params.add(AskParams.Sort.name(), AskSortParam.CONFIDENCE);
            params.add(AskParams.Text.name(), text);
            params.add(AskParams.SystemNames.name(), systemName);
            // These have been added to the answer server config as
            // [Server] AllowedQueryParameters=DatabaseMatch,SecurityInfo,PrintFields
            params.add(QueryParams.DatabaseMatch.name(), entitySearch.getAnswerServerDatabaseMatch());
            params.add(QueryParams.PrintFields.name(), entitySearch.getAnswerServerPrintFields());
            parameterHandler.addSecurityInfo(params);

            if (maxResults != null) {
                params.add(AskParams.MaxResults.name(), maxResults);
            }

            final AskResponsedata answers = aciService.executeAction(details, params, processor);

            final Optional<List<AskAnswer>> list = Optional.ofNullable(answers.getAnswers()).map(AskAnswers::getAnswer);

            if (list.isPresent()) {
                for(AskAnswer answer : list.get()) {
                    toReturn.add(answer);

                    if (maxResults != null && toReturn.size() >= maxResults) {
                        return toReturn;
                    }
                }
            }
        }

        return toReturn;
    }
}
