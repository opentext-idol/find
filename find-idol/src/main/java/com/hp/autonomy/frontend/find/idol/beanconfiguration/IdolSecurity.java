/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthenticationProvider;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLoginAuthenticationProvider;
import com.hp.autonomy.frontend.configuration.authentication.LoginSuccessHandler;
import com.hp.autonomy.frontend.configuration.authentication.OneToOneOrZeroSimpleAuthorityMapper;
import com.hp.autonomy.frontend.configuration.authentication.Role;
import com.hp.autonomy.frontend.configuration.authentication.Roles;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Order(99)
public class IdolSecurity extends WebSecurityConfigurerAdapter {
    public static final String USER_ROLE = "PUBLIC";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String CONFIG_ROLE = "DEFAULT";

    public static final String IDOL_USER_ROLE = "FindUser";
    public static final String IDOL_ADMIN_ROLE = "FindAdmin";

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Autowired
    private UserService userService;

    @Override
    public void configure(final WebSecurity web)
    {
        web.ignoring().antMatchers("/static-*/**");
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth
                .authenticationProvider(new DefaultLoginAuthenticationProvider(configService, CONFIG_ROLE))
                .authenticationProvider(communityAuthenticationProvider());
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final AuthenticationSuccessHandler successHandler = new LoginSuccessHandler(role(CONFIG_ROLE), "/config", FindController.PUBLIC_PATH);

        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(new AntPathRequestMatcher("/api/**"), new Http403ForbiddenEntryPoint());
        entryPoints.put(AnyRequestMatcher.INSTANCE, new LoginUrlAuthenticationEntryPoint("/login"));
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
                    .logoutSuccessUrl("/login")
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticate")
                    .successHandler(successHandler)
                    .failureUrl("/loginPage?error=auth")
                    .and()
                .authorizeRequests()
                    .antMatchers(FindController.PUBLIC_PATH + "/**").hasAnyRole(ADMIN_ROLE, USER_ROLE)
                    .antMatchers("/api/public/**").hasAnyRole(ADMIN_ROLE, USER_ROLE)
                    .antMatchers("/api/config/**").hasRole(CONFIG_ROLE)
                    .antMatchers("/config/**").hasRole(CONFIG_ROLE)
                    .antMatchers("/api/admin/**").hasRole(ADMIN_ROLE)
                    .anyRequest().permitAll()
                    .and()
                .headers()
                    .defaultsDisabled()
                    .frameOptions()
                    .sameOrigin();
    }

    private AuthenticationProvider communityAuthenticationProvider() {
        final Role user = new Role.Builder()
                .setName(IDOL_USER_ROLE)
                .setPrivileges(Collections.singleton("login"))
                .build();

        final Role admin = new Role.Builder()
                .setName(IDOL_ADMIN_ROLE)
                .setParent(Collections.singleton(user))
                .build();

        final Map<String, String> rolesMap = ImmutableMap.<String, String>builder()
                .put(IDOL_USER_ROLE, role(USER_ROLE))
                .put(IDOL_ADMIN_ROLE, role(ADMIN_ROLE))
                .build();

        final Roles roles = new Roles(Arrays.asList(admin, user));

        return new CommunityAuthenticationProvider(
                configService,
                userService,
                roles,
                Collections.singleton("login"),
                new OneToOneOrZeroSimpleAuthorityMapper(rolesMap)
        );
    }

    private String role(final String applicationRole) {
        return "ROLE_" + applicationRole;
    }
}
