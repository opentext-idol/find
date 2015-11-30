/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String USER_ROLE = "PUBLIC";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String CONFIG_ROLE = "DEFAULT";

    @Override
    public void configure(final WebSecurity web) {
        web.ignoring()
                .antMatchers("/static-*/**");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(new AntPathRequestMatcher(FindController.PUBLIC_PATH));

        http
                .authorizeRequests()
                    .antMatchers("/api/public/**").hasRole(USER_ROLE)
                    .antMatchers("/api/useradmin/**").hasRole(ADMIN_ROLE)
                    .antMatchers("/api/config/**").hasRole(CONFIG_ROLE)
                    .and()
                .requestCache()
                    .requestCache(requestCache)
                    .and()
                .csrf()
                    .disable()
                .headers()
                    .defaultsDisabled()
                    .frameOptions()
                    .sameOrigin();
    }
}
