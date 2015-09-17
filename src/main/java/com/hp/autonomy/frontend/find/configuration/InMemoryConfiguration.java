/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.hp.autonomy.hod.client.token.InMemoryTokenRepository;
import com.hp.autonomy.hod.client.token.TokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration
@Conditional(InMemoryCondition.class)
public class InMemoryConfiguration {

    @Bean
    @Conditional(InMemoryCondition.class)
    public SessionRepositoryFilter<ExpiringSession> springSessionRepositoryFilter() {
        return new SessionRepositoryFilter<>(sessionRepository());
    }

    @Bean
    @Conditional(InMemoryCondition.class)
    public SessionRepository<ExpiringSession> sessionRepository() {
        return new MapSessionRepository();
    }

    @Bean
    public TokenRepository tokenRepository() {
        return new InMemoryTokenRepository();
    }

}
