/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLoginAuthenticationProvider;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
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
    private UserService userService;

    @Autowired
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Autowired
    private IdolSecurityCustomizer idolSecurityCustomizer;

    @Override
    public void configure(final WebSecurity web) {
        web.ignoring().antMatchers("/static-*/**");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new DefaultLoginAuthenticationProvider(configService, UserConfiguration.role(UserConfiguration.CONFIG_ROLE)));

        for (final AuthenticationProvider authenticationProvider : idolSecurityCustomizer.getAuthenticationProviders()) {
            auth.authenticationProvider(authenticationProvider);
        }
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(new AntPathRequestMatcher("/api/**"), new Http403ForbiddenEntryPoint());
        entryPoints.put(AnyRequestMatcher.INSTANCE, new LoginUrlAuthenticationEntryPoint("/loginPage"));
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
                    .logoutSuccessUrl("/loginPage")
                    .and()
                .authorizeRequests()
                    .antMatchers(FindController.PUBLIC_PATH + "/**").hasAnyRole(UserConfiguration.ADMIN_ROLE, UserConfiguration.USER_ROLE)
                    .antMatchers(FindController.PRIVATE_PATH + "/**").hasAnyRole(UserConfiguration.ADMIN_ROLE)
                    .antMatchers("/api/public/**").hasAnyRole(UserConfiguration.ADMIN_ROLE, UserConfiguration.USER_ROLE)
                    .antMatchers("/api/config/**").hasRole(UserConfiguration.CONFIG_ROLE)
                    .antMatchers("/config/**").hasRole(UserConfiguration.CONFIG_ROLE)
                    .antMatchers("/api/admin/**").hasRole(UserConfiguration.ADMIN_ROLE)
                    .anyRequest().permitAll()
                    .and()
                .headers()
                    .defaultsDisabled()
                    .frameOptions()
                    .sameOrigin();

        idolSecurityCustomizer.customize(http, authenticationManager());
    }

}
