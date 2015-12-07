/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLoginAuthenticationProvider;
import com.hp.autonomy.frontend.configuration.authentication.LoginSuccessHandler;
import com.hp.autonomy.frontend.configuration.authentication.SingleUserAuthenticationProvider;
import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import com.hp.autonomy.frontend.find.hod.web.SsoController;
import com.hp.autonomy.frontend.find.hod.web.HodLogoutSuccessHandler;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.sso.HodTokenLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@Order(98)
@Conditional(InMemoryCondition.class)
public class InMemoryHodSecurity extends WebSecurityConfigurerAdapter {
    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Autowired
    private TokenRepository tokenRepository;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new DefaultLoginAuthenticationProvider(configService, "ROLE_DEFAULT"));
        auth.authenticationProvider(new SingleUserAuthenticationProvider(configService, "ROLE_ADMIN"));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final AuthenticationSuccessHandler loginSuccessHandler = new LoginSuccessHandler("ROLE_DEFAULT", "/config/", "/p/");
        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

        requestCache.setRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/p/**"),
                new AntPathRequestMatcher("/config/**")
        ));

        http.regexMatcher("/p/.*|/config/.*|/authenticate|/logout")
                .authorizeRequests()
                    .antMatchers("/p/**").hasRole("ADMIN")
                    .antMatchers("/config/**").hasRole("DEFAULT")
                    .and()
                .requestCache()
                    .requestCache(requestCache)
                    .and()
                .formLogin()
                    .loginPage("/loginPage")
                    .loginProcessingUrl("/authenticate")
                    .successHandler(loginSuccessHandler)
                    .failureUrl("/loginPage?error=auth")
                    .and()
                .logout()
                    .logoutSuccessHandler(new HodLogoutSuccessHandler(new HodTokenLogoutSuccessHandler(SsoController.SSO_LOGOUT_PAGE, tokenRepository), "/public/"))
                    .and()
                .csrf()
                    .disable();
    }
}
