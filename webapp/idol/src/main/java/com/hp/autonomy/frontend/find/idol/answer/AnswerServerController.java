/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.answer.ask.AskAnswerServerService;
import com.hp.autonomy.types.idol.responses.answer.AskAnswer;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.util.Collections;
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
    private final ObjectMapper jacksonObjectMapper;
    private final String passageExtractorSystem;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    @Autowired
    AnswerServerController(final AskAnswerServerService askAnswerServerService,
                           final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory,
                           final ObjectMapper jacksonObjectMapper,
                           final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
                           @Value("${questionanswer.system.name.passageExtractor}") final String passageExtractorSystem) {
        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.passageExtractorSystem = passageExtractorSystem;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @RequestMapping(value = ASK_PATH, method = RequestMethod.GET)
    public List<AskAnswer> ask(@RequestParam(TEXT_PARAM)
                               final String text,
                               @RequestParam(value = MAX_RESULTS_PARAM, required = false)
                               final Integer maxResults) throws JsonProcessingException {
        String customizationData = null;

        if (isNotBlank(passageExtractorSystem)) {
            final CommunityPrincipal principal = authenticationInformationRetriever.getPrincipal();
            final String securityInfo = principal.getSecurityInfo();

            if (isNotBlank(securityInfo)) {
                final HashMap<String, String> props = new HashMap<>();
                props.put("system_name", passageExtractorSystem);
                props.put("security_info", securityInfo);
                customizationData = jacksonObjectMapper.writeValueAsString(Collections.singletonList(props));
            }
        }

        final AskAnswerServerRequest request = requestBuilderFactory.getObject()
                .text(text)
                .maxResults(maxResults)
                .customizationData(customizationData)
                .build();

        return askAnswerServerService.ask(request);
    }
}
