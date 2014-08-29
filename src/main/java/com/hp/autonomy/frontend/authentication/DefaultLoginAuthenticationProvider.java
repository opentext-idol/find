package com.hp.autonomy.frontend.authentication;

import com.autonomy.frontend.configuration.AuthenticationConfig;
import com.autonomy.frontend.configuration.ConfigService;
import com.autonomy.frontend.configuration.LoginTypes;
import com.autonomy.frontend.configuration.UsernameAndPassword;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@Service
public class DefaultLoginAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> configService;

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final com.autonomy.frontend.configuration.Authentication<?> authenticationConfig = configService.getConfig().getAuthentication();

        if(!LoginTypes.DEFAULT.equalsIgnoreCase(authenticationConfig.getMethod())) {
            return null;
        }

        final UsernameAndPassword defaultLogin = authenticationConfig.getDefaultLogin();

        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        if(defaultLogin.getUsername().equals(username) && defaultLogin.getPassword().equals(password)) {
            return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList(new SimpleGrantedAuthority("ROLE_DEFAULT")));
        }
        else {
            throw new BadCredentialsException("Access is denied");
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class == authentication;
    }
}
