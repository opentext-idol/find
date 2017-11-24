/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.answer.AskAnswer;
import com.hp.autonomy.types.idol.responses.answer.AskAnswers;
import com.hp.autonomy.types.idol.responses.answer.AskResponsedata;
import com.hp.autonomy.types.requests.idol.actions.answer.params.AskParams;
import com.hp.autonomy.types.requests.idol.actions.answer.params.AskSortParam;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    private final AciService aciService;

    private final HavenSearchAciParameterHandler parameterHandler;

    private final Processor<AskResponsedata> processor;

    private final XPathExpression xAnswer;
    private final XPathExpression xAnswerText;
    private final XPathExpression xScore;
    private final XPathExpression xSource;
    private final XPathExpression xSystemName;
    private final XPathExpression xInterpretation;

    @Autowired
    TempAnswerServerController(final AskAnswerServerService askAnswerServerService,
                               final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory,
                               final AciService aciService,
                               final HavenSearchAciParameterHandler parameterHandler,
                               final ProcessorFactory processorFactory) {
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.aciService = aciService;
        this.parameterHandler = parameterHandler;
        processor = processorFactory.getResponseDataProcessor(AskResponsedata.class);


        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        try {
            xAnswer = xPath.compile("/autnresponse/responsedata/answers/answer");
            xAnswerText = xPath.compile("text");
            xScore = xPath.compile("score");
            xSource = xPath.compile("source");
            xSystemName = xPath.compile("@system_name");
            xInterpretation = xPath.compile("interpretation");
        }
        catch(XPathExpressionException e) {
            throw new Error("Invalid XPaths", e);
        }

    }

    @RequestMapping(value = "ask-demo", method = RequestMethod.GET)
    public List<AskAnswer> ask(
            @RequestParam(TEXT_PARAM) final String text,
           @Value("${temp.answerserver.host}") final String host,
           @Value("${temp.answerserver.port}") final int port,
            @Value("${temp.answerserver.databaseMatch}") final String databaseMatch,
            @Value("${temp.answerserver.printFields}") final String printFields,
           @Value("${temp.answerserver.systemNames}") final List<String> systemNames,
            @RequestParam(value = MAX_RESULTS_PARAM, required = false)
            final Integer maxResults
    ) throws XPathExpressionException {
        final ArrayList<AskAnswer> toReturn = new ArrayList<>();

        if (maxResults != null && maxResults == 0) {
            return toReturn;
        }

        final AciServerDetails details = new AciServerDetails();
        details.setHost(host);
        details.setPort(port);

        for(String systemName : systemNames) {
            // We deliberately do each systemname in sequence, rather than in parallel,
            //   since FactBank is faster than PassageExtraction
            final AciParameters params = new AciParameters("ask");

            params.add(AskParams.Sort.name(), AskSortParam.CONFIDENCE);
            params.add(AskParams.Text.name(), text);
            params.add(AskParams.SystemNames.name(), systemName);
            // These have been added to the answer server config as
            // [Server] AllowedQueryParameters=DatabaseMatch,SecurityInfo,PrintFields
            params.add(QueryParams.DatabaseMatch.name(), databaseMatch);
            params.add(QueryParams.PrintFields.name(), printFields);
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
