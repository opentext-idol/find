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
import com.hp.autonomy.types.idol.responses.answer.GetStatusResponsedata;
import com.hp.autonomy.types.idol.responses.answer.System;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class AnswerServerController {
    static final String BASE_PATH = "/api/public/answer";
    static final String ASK_PATH = "ask";
    static final String TEXT_PARAM = "text";
    static final String FIELDTEXT_PARAM = "fieldText";
    static final String MAX_RESULTS_PARAM = "maxResults";

    private final AskAnswerServerService askAnswerServerService;
    private final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory;
    private final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;
    private final ObjectMapper mapper;

    @Autowired
    AnswerServerController(final AskAnswerServerService askAnswerServerService,
                           final ObjectFactory<AskAnswerServerRequestBuilder> requestBuilderFactory,
                           final AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever,
                           final ObjectMapper mapper
    ) {

        this.askAnswerServerService = askAnswerServerService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.mapper = mapper;
    }

    @RequestMapping(value = ASK_PATH, method = RequestMethod.GET)
    public List<AskAnswer> ask(@RequestParam(TEXT_PARAM)
                               final String text,
                               @RequestParam(FIELDTEXT_PARAM)
                               final String fieldText,
                               @RequestParam(value = MAX_RESULTS_PARAM, required = false)
                               final Integer maxResults) throws JsonProcessingException {
        final ArrayList<Map<String, String>> customizationData = new ArrayList<>();

        if (StringUtils.isNotBlank(fieldText)) {
            final GetStatusResponsedata status = askAnswerServerService.getStatus();
            final String securityInfo = this.authenticationInformationRetriever.getPrincipal() != null
                ? this.authenticationInformationRetriever.getPrincipal().getSecurityInfo() : null;

            for(System system : status.getSystems().getSystem()) {
                if ("passageextractor".equalsIgnoreCase(system.getType())) {
                    final HashMap<String, String> data = new HashMap<>();
                    data.put("system_name", system.getName());
                    data.put("security_info", StringUtils.defaultString(securityInfo));
                    data.put("FieldText", fieldText);
                    customizationData.add(data);
                }
            }
        }

        final AskAnswerServerRequest request = requestBuilderFactory.getObject()
                .text(text)
                .maxResults(maxResults)
                .customizationData(customizationData.isEmpty() ? null : mapper.writeValueAsString(customizationData))
                .build();

        return askAnswerServerService.ask(request);
    }
}
