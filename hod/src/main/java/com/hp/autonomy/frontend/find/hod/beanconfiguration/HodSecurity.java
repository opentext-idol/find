/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.beanconfiguration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.beanconfiguration.FindRole;
import com.hp.autonomy.frontend.find.core.web.FindController;
import com.hp.autonomy.frontend.find.hod.web.SsoController;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.authentication.tokeninformation.GroupInformation;
import com.hp.autonomy.hod.client.api.userstore.user.UserStoreUsersService;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.sso.HodAuthenticationProvider;
import com.hp.autonomy.hod.sso.HodTokenLogoutSuccessHandler;
import com.hp.autonomy.hod.sso.HodUsernameResolver;
import com.hp.autonomy.hod.sso.SsoAuthenticationEntryPoint;
import com.hp.autonomy.hod.sso.SsoAuthenticationFilter;
import com.hp.autonomy.hod.sso.UnboundTokenService;
import com.hp.autonomy.searchcomponents.hod.authentication.HavenSearchUserMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@Order(99)
public class HodSecurity extends WebSecurityConfigurerAdapter {
    private static final String HOD_BI_ROLE = "bi";

    @Value("${" + BiConfiguration.BI_PROPERTY + '}')
    private boolean enableBi;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UnboundTokenService<TokenType.HmacSha1> unboundTokenService;

    @Autowired
    private UserStoreUsersService userStoreUsersService;

    @Autowired
    private HodUsernameResolver hodUsernameResolverImpl;

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new HodAuthenticationProvider(
                tokenRepository,
                (tokenProxy, combinedTokenInformation) -> {
                    final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>(2);
                    grantedAuthorities.add(new SimpleGrantedAuthority(FindRole.USER.toString()));

                    for (final GroupInformation groupInformation : combinedTokenInformation.getUser().getGroups()) {
                        if (enableBi && groupInformation.getGroups().contains(HOD_BI_ROLE)) {
                            grantedAuthorities.add(new SimpleGrantedAuthority(FindRole.BI.toString()));
                            break;
                        }
                    }

                    return grantedAuthorities;
                },
                authenticationService,
                unboundTokenService,
                userStoreUsersService,
                HavenSearchUserMetadata.METADATA_TYPES,
                hodUsernameResolverImpl,
                null
        ));
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final AuthenticationEntryPoint ssoEntryPoint = new SsoAuthenticationEntryPoint(SsoController.SSO_PAGE);

        final SsoAuthenticationFilter ssoAuthenticationFilter = new SsoAuthenticationFilter(SsoController.SSO_AUTHENTICATION_URI);
        ssoAuthenticationFilter.setAuthenticationManager(authenticationManager());

        final LogoutSuccessHandler logoutSuccessHandler = new HodTokenLogoutSuccessHandler(SsoController.SSO_LOGOUT_PAGE, tokenRepository);

        http.regexMatcher("/public/.*|/sso|/authenticate-sso|/api/authentication/.*|/logout")
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(ssoEntryPoint)
                .accessDeniedPage(DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH)
                .and()
                .authorizeRequests()
                .antMatchers(FindController.APP_PATH + "**").hasRole(FindRole.USER.name())
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                .addFilterAfter(ssoAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class);
    }
}
