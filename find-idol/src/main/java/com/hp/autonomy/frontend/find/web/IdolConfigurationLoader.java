/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.web;

import com.hp.autonomy.frontend.find.core.beanconfiguration.ConfigurationLoader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@Conditional(IdolCondition.class)
@ComponentScan("com.hp.autonomy.frontend.find.idol")
public class IdolConfigurationLoader implements ConfigurationLoader {
    @Override
    public boolean isHosted() {
        return false;
    }
}
