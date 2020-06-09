/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.data.domain.AuditorAware;

import java.security.Principal;

/**
 * Implements {@link AuditorAware<UserEntity>} which means it will automatically be picked up by
 * spring jpa via {@link org.springframework.data.jpa.domain.support.AuditingEntityListener}.
 *
 * Defines the current auditor, that is the user to be added to the field annotated with {@link org.springframework.data.annotation.CreatedBy}.
 *
 * Abstract so that a {@link UserEntity} can be constructed for various principals.
 */
public abstract class AbstractFindSpringSecurityAuditorAware<P extends Principal> implements AuditorAware<UserEntity> {
    private final AuthenticationInformationRetriever<?, P> authenticationInformationRetriever;
    private final UserEntityRepository userRepository;

    protected AbstractFindSpringSecurityAuditorAware(
            final AuthenticationInformationRetriever<?, P> authenticationInformationRetriever,
            final UserEntityRepository userRepository
    ) {
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.userRepository = userRepository;
    }

    /**
     * Translate the various principal objects into a unified entity that can be persisted
     */
    protected abstract UserEntity principalToUser(P principal);

    /**
     * Return the current user as a {@link UserEntity} to be inserted into a {@link org.springframework.data.annotation.CreatedBy} field.
     */
    @Override
    public UserEntity getCurrentAuditor() {
        final P principal = authenticationInformationRetriever.getPrincipal();

        if(principal == null) {
            return null;
        }

        final UserEntity currentUser = principalToUser(principal);

        return userRepository.findByUsername(
                currentUser.getUsername()
        );
    }
}
