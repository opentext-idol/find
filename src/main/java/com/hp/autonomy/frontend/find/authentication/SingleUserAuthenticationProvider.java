/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.authentication;

import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.BCryptUsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.SingleUserAuthentication;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class SingleUserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final com.hp.autonomy.frontend.configuration.Authentication<?> configAuthentication = configService.getConfig().getAuthentication();

        if(!(configAuthentication instanceof SingleUserAuthentication)) {
            return null;
        }

        final SingleUserAuthentication singleUserAuthentication = (SingleUserAuthentication) configAuthentication;
        final BCryptUsernameAndPassword singleUser = singleUserAuthentication.getSingleUser();

        final String username = singleUser.getUsername();
        final String hashedPassword = singleUser.getHashedPassword();
        final String providedPassword = authentication.getCredentials().toString();

        if(authentication.getName().equals(username) && BCrypt.checkpw(providedPassword, hashedPassword)) {
            return new UsernamePasswordAuthenticationToken(username, providedPassword, Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }
        else {
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication == UsernamePasswordAuthenticationToken.class;
    }
}
