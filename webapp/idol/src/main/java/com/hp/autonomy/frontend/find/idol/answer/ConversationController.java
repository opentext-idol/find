/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationRequestBuilder;
import com.hp.autonomy.types.idol.responses.conversation.ConversePrompt;
import java.util.List;
import lombok.Data;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class ConversationController {
    static final String CONVERSE_PATH = "converse";
    static final String TEXT_PARAM = "text";
    static final String SESSION_ID_PARAM = "sessionId";
    static final String CONVERSE_END_PATH = "converse/{"+SESSION_ID_PARAM+"}/end";

    private final ConversationAnswerServerService conversationService;
    private final ObjectFactory<ConversationRequestBuilder> requestBuilderFactory;

    @Autowired
    ConversationController(final ConversationAnswerServerService conversationService,
                           final ObjectFactory<ConversationRequestBuilder> requestBuilderFactory) {
        this.conversationService = conversationService;
        this.requestBuilderFactory = requestBuilderFactory;
    }

    @RequestMapping(value = CONVERSE_PATH, method = RequestMethod.POST)
    public Conversation converse(@RequestParam(TEXT_PARAM)
                               final String text,
                                 @RequestParam(value = SESSION_ID_PARAM, required = false)
                               String sessionId) {

        if (sessionId == null) {
            // TODO: security, and cleanup
            sessionId = conversationService.conversationStart();

            if (sessionId == null) {
                throw new Error("Unable to start conversation");
            }
        }

        final ConversationRequest request = requestBuilderFactory.getObject()
                .text(text)
                .sessionId(sessionId)
                .build();

        return new Conversation(conversationService.converse(request), sessionId);
    }

    @RequestMapping(value = CONVERSE_END_PATH, method = RequestMethod.POST)
    public boolean converseEnd(
            @PathVariable(SESSION_ID_PARAM) final String sessionId) {

        // TODO: security, and cleanup
        conversationService.conversationEnd(sessionId);

        return true;
    }

    @Data
    public static class Conversation {
        final List<ConversePrompt> prompts;
        final String sessionId;
    }
}
