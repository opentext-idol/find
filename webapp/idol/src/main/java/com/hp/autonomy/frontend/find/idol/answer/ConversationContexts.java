/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.answer;

import java.util.LinkedHashMap;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ConversationContexts extends LinkedHashMap<String, ConversationContexts.ConversationContext> {

    @Data
    public static class ConversationContext {
        private final String sessionId;
        private boolean terminated;
    }
}
