/*
 * Copyright 2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.idol.api.ConversationApi;
import com.hp.autonomy.frontend.find.idol.api.model.Conversation;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationAnswerServerService;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationRequest;
import com.hp.autonomy.searchcomponents.idol.answer.ask.ConversationRequestBuilder;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
class ConversationController implements ConversationApi {
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

    @Override
    public ResponseEntity<Conversation> converse(final String text, String sessionId) {
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

        return ResponseEntity.ok(new Conversation(conversationService.converse(request), sessionId));
    }

    @Override
    public ResponseEntity<Boolean> converseEnd(final String sessionId) {
        final ConversationContexts.ConversationContext context = contexts.get(sessionId);

        if (context == null || context.isTerminated()) {
            return ResponseEntity.ok(false);
        }

        conversationService.conversationEnd(sessionId);
        context.setTerminated(true);
        contexts.remove(sessionId);

        return ResponseEntity.ok(true);
    }

}
