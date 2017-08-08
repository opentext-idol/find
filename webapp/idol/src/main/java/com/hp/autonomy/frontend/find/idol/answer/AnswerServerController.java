/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.types.idol.responses.answer.AskAnswer;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class AnswerServerController {
    static final String BASE_PATH = "/api/public/answer";
    static final String ASK_PATH = "ask";
    static final String TEXT_PARAM = "text";
    static final String MAX_RESULTS_PARAM = "maxResults";

    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    private final String questionAnswerDatabaseMatch;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    @Autowired
    AnswerServerController(final AskAnswerServerService askAnswerServerService,
                           final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory,
                           final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
                           @Value("${questionanswer.databaseMatch}") final String questionAnswerDatabaseMatch) {
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.questionAnswerDatabaseMatch = questionAnswerDatabaseMatch;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @RequestMapping(value = ASK_PATH, method = RequestMethod.GET)
    public List<AskAnswer> ask(@RequestParam(TEXT_PARAM)
                               final String text,
                               @RequestParam(value = MAX_RESULTS_PARAM, required = false)
                               final Integer maxResults) {
        final HashMap<String, String> extraParams = new HashMap<>();

        final CommunityPrincipal principal = authenticationInformationRetriever.getPrincipal();
        final String securityInfo = principal.getSecurityInfo();

        if (isNotBlank(securityInfo)) {
            extraParams.put("securityInfo", securityInfo);
        }

        if (isNotBlank(questionAnswerDatabaseMatch)) {
            extraParams.put("databaseMatch", questionAnswerDatabaseMatch);
        }

        final AskAnswerServerRequest request = requestBuilderFactory.getObject()
                .text(text)
                .maxResults(maxResults)
                .proxiedParams(extraParams)
                .build();

        return askAnswerServerService.ask(request);
    }
}
