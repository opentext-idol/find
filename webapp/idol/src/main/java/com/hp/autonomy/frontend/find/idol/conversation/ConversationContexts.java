/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.conversation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import static com.hp.autonomy.frontend.find.idol.conversation.ConversationContexts.PassageExtractionState.DISABLED;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ConversationContexts extends LinkedHashMap<String, ConversationContexts.ConversationContext> {

    @Data
    public static class ConversationContext {

        private final List<Utterance> history = new ArrayList<>();

        private PassageExtractionState passageExtractionMode = DISABLED;
    }

    enum PassageExtractionState {
        PREQUERY,
        POSTQUERY,
        DISABLED
    }
}
