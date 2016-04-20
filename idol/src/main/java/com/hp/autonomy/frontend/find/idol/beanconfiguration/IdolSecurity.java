/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLoginAuthenticationProvider;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
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
        web.ignoring().antMatchers("/static-*/**");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new DefaultLoginAuthenticationProvider(configService, FindRole.CONFIG.toString()));

        for (final AuthenticationProvider authenticationProvider : idolSecurityCustomizer.getAuthenticationProviders()) {
            auth.authenticationProvider(authenticationProvider);
        }
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
                    .antMatchers(FindController.APP_PATH + "**").hasAnyRole(FindRole.ADMIN.name(), FindRole.USER.name())
                    .antMatchers(FindController.CONFIG_PATH).hasRole(FindRole.CONFIG.name())
                    .antMatchers("/api/public/**").hasAnyRole(FindRole.ADMIN.name(), FindRole.USER.name())
                    .antMatchers("/api/config/**").hasRole(FindRole.CONFIG.name())
                    .antMatchers("/api/admin/**").hasRole(FindRole.ADMIN.name())
                    .antMatchers(FindController.DEFAULT_LOGIN_PAGE).permitAll()
                    .antMatchers(FindController.LOGIN_PATH).permitAll()
                    .antMatchers("/").permitAll()
                    .anyRequest().denyAll()
                    .and()
                .headers()
                    .defaultsDisabled()
                    .frameOptions()
                    .sameOrigin();

        idolSecurityCustomizer.customize(http, authenticationManager());
    }

}
