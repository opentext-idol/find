/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration
@Conditional(NotRedisCondition.class)
public class SessionConfiguration {

    @Bean
    @Conditional(NotRedisCondition.class)
    public SessionRepositoryFilter<ExpiringSession> springSessionRepositoryFilter() {
        return new SessionRepositoryFilter<>(sessionRepository());
    }

    @Bean
    @Conditional(NotRedisCondition.class)
    public SessionRepository<ExpiringSession> sessionRepository() {
        return new MapSessionRepository();
    }

}
