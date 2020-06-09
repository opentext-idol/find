/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthenticationProvider;
import com.hp.autonomy.frontend.configuration.authentication.Role;
import com.hp.autonomy.frontend.configuration.authentication.Roles;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.idol.authentication.FindCommunityRole;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Component
@ConditionalOnProperty(value = "server.reverseProxy", havingValue = "false", matchIfMissing = true)
public class IdolSecurityCustomizerImpl implements IdolSecurityCustomizer {

    static final String DEFAULT_ROLES_KEY = "find.defaultRoles";

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Autowired
    private UserService userService;

    @Autowired
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @Autowired
    private AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever;

    @Value("${" + DEFAULT_ROLES_KEY + '}')
    private String defaultRolesProperty;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public void customize(final HttpSecurity http, final AuthenticationManager authenticationManager) throws Exception {
        final AuthenticationSuccessHandler successHandler = new IdolLoginSuccessHandler(
                FindController.CONFIG_PATH,
                FindController.APP_PATH,
                FindRole.CONFIG.toString(),
                authenticationInformationRetriever
        );

        http.formLogin()
                .loginPage(FindController.DEFAULT_LOGIN_PAGE)
                .loginProcessingUrl("/authenticate")
                .successHandler(successHandler)
                .failureUrl(FindController.DEFAULT_LOGIN_PAGE + "?error=auth");
    }

    @Override
    public Collection<AuthenticationProvider> getAuthenticationProviders() {
        return Collections.singleton(communityAuthenticationProvider());
    }

    private AuthenticationProvider communityAuthenticationProvider() {
        final Role user = new Role.Builder()
                .setName(FindCommunityRole.USER.value())
                .setPrivileges(Collections.singleton("login"))
                .build();

        final Set<String> defaultRoles;

        if (defaultRolesProperty.isEmpty()) {
            defaultRoles = Collections.emptySet();
        } else {
            defaultRoles = new HashSet<>(Arrays.asList(defaultRolesProperty.split(",")));
        }

        return new CommunityAuthenticationProvider(
                configService,
                userService,
                new Roles(Collections.singletonList(user)),
                Collections.singleton("login"),
                grantedAuthoritiesMapper,
                defaultRoles
        );
    }
}
