/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.authentication;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.idol.beanconfiguration.UserConfiguration;
import com.hp.autonomy.user.UserRoles;
import com.hp.autonomy.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;

@Component
@Slf4j
@ConditionalOnProperty("server.reverseProxy")
public class IdolPreAuthenticatedAuthenticationProvider implements AuthenticationProvider {

    private static final String USER_NOT_FOUND_ERROR_ID = "UASERVERUSERREAD-2147438053";

    private final UserService userService;
    private final GrantedAuthoritiesMapper authoritiesMapper;

    @Autowired
    public IdolPreAuthenticatedAuthenticationProvider(final UserService userService, final GrantedAuthoritiesMapper authoritiesMapper) {
        this.userService = userService;
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final Object principal = authentication.getPrincipal();

        if (principal == null) {
            throw new BadCredentialsException("Principal not supplied");
        }

        final String username = principal.toString().toLowerCase();

        UserRoles user;

        try {
            user = userService.getUser(username);
        } catch(final AciErrorException e) {
            log.debug("Failed to fetch the user", e);

            if(USER_NOT_FOUND_ERROR_ID.equals(e.getErrorId())) {
                // use empty password so that auto created users cannot be authenticated against
                this.userService.addUser(username, "", UserConfiguration.IDOL_USER_ROLE);

                user = userService.getUser(username);
            } else {
                throw e;
            }
        }

        final Collection<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

        for (final String role : user.getRoles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role));
        }

        return new UsernamePasswordAuthenticationToken(new CommunityPrincipal(user.getUid(), username, user.getSecurityInfo()), null, authoritiesMapper.mapAuthorities(grantedAuthorities));
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }

}
