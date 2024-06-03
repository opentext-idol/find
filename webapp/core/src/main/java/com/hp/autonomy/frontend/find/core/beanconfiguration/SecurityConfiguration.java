/*
 * Copyright 2015-2017 Open Text.
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

package com.hp.autonomy.frontend.find.core.beanconfiguration;

import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() throws Exception {
        return web -> web.httpFirewall(firewallAllowingUrlEncodedCharacters())
            .ignoring()
            .requestMatchers("/static-*/**")
            .requestMatchers("/customization/**");
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

    @Bean
    protected SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(new AntPathRequestMatcher(FindController.APP_PATH + "/**"));

        http
            .authorizeRequests()
                .requestMatchers("/api/public/**").hasRole(FindRole.USER.name())
                .requestMatchers("/api/admin/**").hasRole(FindRole.ADMIN.name())
                .requestMatchers("/api/config/**").hasRole(FindRole.CONFIG.name())
                .requestMatchers("/api/bi/**").hasRole(FindRole.BI.name())
                .and()
            .requestCache(r -> r.requestCache(requestCache))
            .csrf(c -> c.disable())
            .headers(h -> h
                .defaultsDisabled()
                .frameOptions(f -> f.sameOrigin())
                .contentSecurityPolicy(c -> c.policyDirectives("frame-ancestors 'self'"))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
            );
        return http.build();
    }
}
