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

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLoginAuthenticationProvider;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

import static com.hp.autonomy.frontend.find.core.beanconfiguration.SecurityConfiguration.firewallAllowingUrlEncodedCharacters;

@Configuration
@Order(99)
public class IdolSecurity extends WebSecurityConfigurerAdapter {
    @Value("${server.reverseProxy}")
    private boolean reverseProxy;

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Autowired
    private IdolSecurityCustomizer idolSecurityCustomizer;

    @Override
    public void configure(final WebSecurity web) {
        web.httpFirewall(firewallAllowingUrlEncodedCharacters())
            .ignoring()
            .antMatchers("/static-*/**")
            .antMatchers("/customization/**");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new DefaultLoginAuthenticationProvider(configService, FindRole.CONFIG.toString()));

        idolSecurityCustomizer.getAuthenticationProviders().forEach(auth::authenticationProvider);
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(new AntPathRequestMatcher("/api/**"), new Http403ForbiddenEntryPoint());
        entryPoints.put(AnyRequestMatcher.INSTANCE, new LoginUrlAuthenticationEntryPoint(FindController.DEFAULT_LOGIN_PAGE));
        final AuthenticationEntryPoint authenticationEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);

        http
            .csrf()
                .disable()
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedPage("/authentication-error")
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl(FindController.DEFAULT_LOGIN_PAGE)
                .and()
            .authorizeRequests()
                .antMatchers(FindController.APP_PATH + "/**").hasAnyRole(FindRole.USER.name())
                .antMatchers(FindController.CONFIG_PATH).hasRole(FindRole.CONFIG.name())
                .antMatchers("/api/public/**").hasRole(FindRole.USER.name())
                .antMatchers("/api/bi/**").hasRole(FindRole.BI.name())
                .antMatchers("/api/config/**").hasRole(FindRole.CONFIG.name())
                .antMatchers("/api/admin/**").hasRole(FindRole.ADMIN.name())
                .antMatchers(FindController.DEFAULT_LOGIN_PAGE).permitAll()
                .antMatchers(FindController.LOGIN_PATH).permitAll()
                .antMatchers("/").permitAll()
                .anyRequest().denyAll()
                .and()
            .headers()
                .defaultsDisabled()
                .frameOptions().sameOrigin()
                .contentSecurityPolicy("frame-ancestors 'self'");

        idolSecurityCustomizer.customize(http, authenticationManager());
    }
}
