/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationRequestBuilder;
import com.hp.autonomy.types.idol.responses.conversation.ConversePrompt;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(AnswerServerController.BASE_PATH)
class ConversationController {
    static final String CONVERSE_PATH = "converse";
    static final String TEXT_PARAM = "text";
    static final String SESSION_ID_PARAM = "sessionId";
    static final String CONVERSE_END_PATH = "converse/{"+SESSION_ID_PARAM+"}/end";

    private final ConversationAnswerServerService conversationService;
    private final ObjectFactory<ConversationRequestBuilder> requestBuilderFactory;
    private final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever;
    private final ConversationContexts contexts;

    @Autowired
    ConversationController(final ConversationAnswerServerService conversationService,
                           final ObjectFactory<ConversationRequestBuilder> requestBuilderFactory,
                           final AuthenticationInformationRetriever<?, ? extends Principal> authenticationInformationRetriever,
                           final ConversationContexts contexts) {
        this.conversationService = conversationService;
        this.requestBuilderFactory = requestBuilderFactory;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.contexts = contexts;
    }

    @RequestMapping(value = CONVERSE_PATH, method = RequestMethod.POST)
    public Conversation converse(@RequestParam(TEXT_PARAM)
                               final String text,
                                 @RequestParam(value = SESSION_ID_PARAM, required = false)
                               String sessionId) {
        final boolean illegalSessionId = sessionId != null && !contexts.containsKey(sessionId);
        if (illegalSessionId) {
            // The user is trying to use a dialog ID which doesn't belong to their session.
            log.warn("User {} tried to access a context ID {} which doesn't belong to them.", authenticationInformationRetriever.getPrincipal().getName(), sessionId);
        }

        if (sessionId == null || illegalSessionId) {
            final Principal principal = this.authenticationInformationRetriever.getPrincipal();

            final Map<String, String> properties = new HashMap<>();
            properties.put("USER_NAME", principal.getName());

            if (principal instanceof CommunityPrincipal) {
                final String securityInfo = ((CommunityPrincipal) principal).getSecurityInfo();
                if (StringUtils.isNotBlank(securityInfo)) {
                    properties.put("SECURITY_INFO", securityInfo);
                }
            }

            sessionId = conversationService.conversationStart(properties);

            if (sessionId == null) {
                throw new Error("Unable to start conversation");
            }

            contexts.put(sessionId, new ConversationContexts.ConversationContext(sessionId));
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
        final ConversationContexts.ConversationContext context = contexts.get(sessionId);

        if (context == null || context.isTerminated()) {
            return false;
        }

        conversationService.conversationEnd(sessionId);
        context.setTerminated(true);
        contexts.remove(sessionId);

        return true;
    }

    @Data
    public static class Conversation {
        final List<ConversePrompt> prompts;
        final String sessionId;
    }
}
