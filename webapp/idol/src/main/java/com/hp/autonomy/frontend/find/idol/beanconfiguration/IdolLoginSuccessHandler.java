/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.beanconfiguration;

import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IdolLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final String configUrl;
    private final String applicationUrl;
    private final String roleDefault;
    private final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever;
    private final UserEntityRepository userEntityRepository;

    private final Object newUserLock = new Object();

    IdolLoginSuccessHandler(
            final String configUrl,
            final String applicationUrl,
            final String roleDefault,
            final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever,
            final UserEntityRepository userEntityRepository
    ) {
        this.configUrl = configUrl;
        this.applicationUrl = applicationUrl;
        this.roleDefault = roleDefault;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.userEntityRepository = userEntityRepository;
    }

    // Check if we have a user entity for the current user in the database, if not, create one.
    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws ServletException, IOException {
        if (authentication.getPrincipal() instanceof CommunityPrincipal) {
            final CommunityPrincipal principal = (CommunityPrincipal) authenticationInformationRetriever.getPrincipal();
            final String principalUsername = principal.getUsername();

            final UserEntity persistedUser = userEntityRepository.findByUsername(principalUsername);

            if (persistedUser == null) {
                // Ensure if, say, two applications log in as the same user at same time, one will be made to wait for lock.
                synchronized (newUserLock) {
                    // Check that there is still no existing user entity after lock is released.
                    final UserEntity persistedUser2 = userEntityRepository.findByUsername(principalUsername);

                    if (persistedUser2 == null) {
                        final UserEntity currentUser = new UserEntity();
                        currentUser.setUsername(principalUsername);

                        userEntityRepository.save(currentUser);
                    }
                }
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response) {
        final Authentication authentication = authenticationInformationRetriever.getAuthentication();

        for (final GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            final String authority = grantedAuthority.getAuthority();

            if (roleDefault.equalsIgnoreCase(authority)) {
                return configUrl;
            }
        }

        return applicationUrl;
    }
}
