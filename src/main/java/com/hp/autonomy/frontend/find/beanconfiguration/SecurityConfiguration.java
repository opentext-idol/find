/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.beanconfiguration;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.authentication.DefaultLoginAuthenticationProvider;
import com.hp.autonomy.frontend.configuration.authentication.LoginSuccessHandler;
import com.hp.autonomy.frontend.configuration.authentication.SingleUserAuthenticationProvider;
import com.hp.autonomy.frontend.find.HodFindController;
import com.hp.autonomy.frontend.find.authentication.HavenSearchUserMetadata;
import com.hp.autonomy.frontend.find.authentication.HsodUsernameResolver;
import com.hp.autonomy.frontend.find.web.HodLogoutSuccessHandler;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.userstore.user.UserStoreUsersService;
import com.hp.autonomy.hod.client.token.TokenRepository;
import com.hp.autonomy.hod.sso.HodAuthenticationProvider;
import com.hp.autonomy.hod.sso.HodTokenLogoutSuccessHandler;
import com.hp.autonomy.hod.sso.SsoAuthenticationEntryPoint;
import com.hp.autonomy.hod.sso.SsoAuthenticationFilter;
import com.hp.autonomy.hod.sso.UnboundTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Configuration
    public static class AppSecurity extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(final WebSecurity web) {
            web.ignoring()
                .antMatchers("/static-*/**");
        }

        @SuppressWarnings("ProhibitedExceptionDeclared")
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                    .antMatchers("/api/public/**").hasRole("PUBLIC")
                    .antMatchers("/api/useradmin/**").hasRole("ADMIN")
                    .antMatchers("/api/config/**").hasRole("DEFAULT")
                    .and()
                .csrf()
                    .disable()
                .headers()
                    .defaultsDisabled()
                    .frameOptions()
                        .sameOrigin();
        }
    }

    @Configuration
    @Conditional(IdolCondition.class)
    @Order(99)
    public static class IdolSecurity extends WebSecurityConfigurerAdapter {



    }

    @Configuration
    @Conditional(HodCondition.class)
    @Order(99)
    public static class HodSecurity extends WebSecurityConfigurerAdapter {

        @Autowired
        private TokenRepository tokenRepository;

        @Autowired
        private AuthenticationService authenticationService;

        @Autowired
        private UnboundTokenService<TokenType.HmacSha1> unboundTokenService;

        @Autowired
        private UserStoreUsersService userStoreUsersService;

        @SuppressWarnings("ProhibitedExceptionDeclared")
        @Override
        protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(new HodAuthenticationProvider(
                tokenRepository,
                "ROLE_PUBLIC",
                authenticationService,
                unboundTokenService,
                userStoreUsersService,
                HavenSearchUserMetadata.METADATA_TYPES,
                usernameResolver()
            ));
        }

        @SuppressWarnings("ProhibitedExceptionDeclared")
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            final AuthenticationEntryPoint ssoEntryPoint = new SsoAuthenticationEntryPoint(HodFindController.SSO_PAGE);

            final SsoAuthenticationFilter ssoAuthenticationFilter = new SsoAuthenticationFilter(HodFindController.SSO_AUTHENTICATION_URI);
            ssoAuthenticationFilter.setAuthenticationManager(authenticationManager());

            final LogoutSuccessHandler logoutSuccessHandler = new HodTokenLogoutSuccessHandler(HodFindController.SSO_LOGOUT_PAGE, tokenRepository);

            http.regexMatcher("/public/.*|/sso|/authenticate-sso|/api/authentication/.*|/logout")
                .csrf()
                    .disable()
                .exceptionHandling()
                    .authenticationEntryPoint(ssoEntryPoint)
                        .accessDeniedPage("/authentication-error")
                        .and()
                .authorizeRequests()
                    .antMatchers("/public/**").hasRole("PUBLIC")
                    .and()
                .logout()
                    .logoutSuccessHandler(logoutSuccessHandler)
                    .and()
                .addFilterAfter(ssoAuthenticationFilter, AbstractPreAuthenticatedProcessingFilter.class);
        }

        @Bean
        public HsodUsernameResolver usernameResolver() {
            return new HsodUsernameResolver();
        }

    }

    @Configuration
    @Order(98)
    @Conditional({HodCondition.class, InMemoryCondition.class})
    public static class InMemoryHodSecurity extends WebSecurityConfigurerAdapter {
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
            final LoginSuccessHandler loginSuccessHandler = new LoginSuccessHandler("ROLE_DEFAULT", "/config/", "/p/");

            http.regexMatcher("/p/.*|/config/.*|/authenticate|/logout")
                .authorizeRequests()
                    .antMatchers("/p/**").hasRole("ADMIN")
                    .antMatchers("/config/**").hasRole("DEFAULT")
                    .and()
                .formLogin()
                    .loginPage("/loginPage")
                    .loginProcessingUrl("/authenticate")
                    .successHandler(loginSuccessHandler)
                    .failureUrl("/loginPage?error=auth")
                    .and()
                .logout()
                    .logoutSuccessHandler(new HodLogoutSuccessHandler(new HodTokenLogoutSuccessHandler(HodFindController.SSO_LOGOUT_PAGE, tokenRepository), "/public/"))
                .and()
                .csrf()
                    .disable();
        }
    }



}
