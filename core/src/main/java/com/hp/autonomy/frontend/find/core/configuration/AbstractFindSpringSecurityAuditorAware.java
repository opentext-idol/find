/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;


import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import org.springframework.data.domain.AuditorAware;

import java.security.Principal;

/**
 * Implements {@link AuditorAware<UserEntity>} which means it will automatically be picked up by
 * spring jpa via {@link org.springframework.data.jpa.domain.support.AuditingEntityListener}.
 * <p/>
 * Defines the current auditor, that is the user to be added to the field annotated with {@link org.springframework.data.annotation.CreatedBy}.
 * <p/>
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
    public UserEntity getCurrentAuditor() {
        final P principal = authenticationInformationRetriever.getPrincipal();

        if (principal == null) {
            return null;
        }

        UserEntity currentUser = principalToUser(principal);

        final UserEntity persistedUser = userRepository.findByDomainAndUserStoreAndUuidAndUid(
                currentUser.getDomain(),
                currentUser.getUserStore(),
                currentUser.getUuid(),
                currentUser.getUid()
        );

        if (persistedUser != null) {
            currentUser = persistedUser;
        } else {
            userRepository.save(currentUser);
        }

        return currentUser;
    }
}
