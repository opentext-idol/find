/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(final WebSecurity web) {
        web.httpFirewall(firewallAllowingUrlEncodedCharacters())
            .ignoring()
            .antMatchers("/static-*/**")
            .antMatchers("/customization/**");
    }

    public static StrictHttpFirewall firewallAllowingUrlEncodedCharacters() {
        final StrictHttpFirewall firewall = new StrictHttpFirewall();

        // We use encoded IDOL field names, e.g.
        //   'api/public/parametric/numeric/buckets/NODE_PLACE%252FPLACE_POPULATION'
        // so we have to allow these fields through.
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPeriod(true);
        firewall.setAllowUrlEncodedSlash(true);

        return firewall;
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(new AntPathRequestMatcher(FindController.APP_PATH + "/**"));

        http
            .authorizeRequests()
                .antMatchers("/api/public/**").hasRole(FindRole.USER.name())
                .antMatchers("/api/admin/**").hasRole(FindRole.ADMIN.name())
                .antMatchers("/api/config/**").hasRole(FindRole.CONFIG.name())
                .antMatchers("/api/bi/**").hasRole(FindRole.BI.name())
                .and()
            .requestCache()
                .requestCache(requestCache)
                .and()
            .csrf()
                .disable()
            .headers()
                .defaultsDisabled()
                .frameOptions().sameOrigin()
                .contentSecurityPolicy("frame-ancestors 'self'");
    }
}
