/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthenticationProvider;
import com.hp.autonomy.frontend.configuration.authentication.LoginSuccessHandler;
import com.hp.autonomy.frontend.configuration.authentication.Role;
import com.hp.autonomy.frontend.configuration.authentication.Roles;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import com.hp.autonomy.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
@ConditionalOnProperty(value = "server.reverseProxy", havingValue = "false", matchIfMissing = true)
public class IdolSecurityCustomizerImpl implements IdolSecurityCustomizer {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Autowired
    private UserService userService;

    @Autowired
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Autowired
    private AuthenticationInformationRetriever<?> authenticationInformationRetriever;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void customize(final HttpSecurity http, final AuthenticationManager authenticationManager) throws Exception {
        final AuthenticationSuccessHandler successHandler = new IdolLoginSuccessHandler(
                "/config",
                FindController.PUBLIC_PATH,
                FindController.PRIVATE_PATH,
                UserConfiguration.role(UserConfiguration.CONFIG_ROLE),
                UserConfiguration.role(UserConfiguration.ADMIN_ROLE),
                authenticationInformationRetriever
        );

        http.formLogin()
            .loginPage("/loginPage")
            .loginProcessingUrl("/authenticate")
            .successHandler(successHandler)
            .failureUrl("/loginPage?error=auth");
    }

    @Override
    public Collection<AuthenticationProvider> getAuthenticationProviders() {
        return Collections.singleton(communityAuthenticationProvider());
    }

    private AuthenticationProvider communityAuthenticationProvider() {
        final Role user = new Role.Builder()
            .setName(UserConfiguration.IDOL_USER_ROLE)
            .setPrivileges(Collections.singleton("login"))
            .build();

        final Role admin = new Role.Builder()
            .setName(UserConfiguration.IDOL_ADMIN_ROLE)
            .setParent(Collections.singleton(user))
            .build();

        final Roles roles = new Roles(Arrays.asList(admin, user));

        return new CommunityAuthenticationProvider(
            configService,
            userService,
            roles,
            Collections.singleton("login"),
            grantedAuthoritiesMapper
        );
    }
}
