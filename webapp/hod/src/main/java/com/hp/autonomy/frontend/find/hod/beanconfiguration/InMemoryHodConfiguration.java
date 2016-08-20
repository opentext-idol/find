/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import com.hp.autonomy.hod.client.token.InMemoryTokenRepository;
import com.hp.autonomy.hod.client.token.TokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Beans which only need to exist when using Haven OnDemand without Redis
 */
@Configuration
@Conditional(InMemoryCondition.class)
public class InMemoryHodConfiguration {

    @Bean
    public TokenRepository tokenRepository() {
        return new InMemoryTokenRepository();
    }

}
