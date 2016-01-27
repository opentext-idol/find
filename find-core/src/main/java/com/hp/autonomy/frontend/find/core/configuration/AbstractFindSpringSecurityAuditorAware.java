/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;


import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Implements {@link AuditorAware<UserEntity>} which means it will automatically be picked up by
 * spring jpa via {@link org.springframework.data.jpa.domain.support.AuditingEntityListener}.
 *
 * Defines the current auditor, that is the user to be added to the field annotated with {@link org.springframework.data.annotation.CreatedBy}.
 *
 * Abstract so that a {@link UserEntity} can be constructed for various principals.
 */
public abstract class AbstractFindSpringSecurityAuditorAware implements AuditorAware<UserEntity> {

    /**
     * Translate the various principal objects into a unified entity that can be persisted
     */
    protected abstract UserEntity principalToUser(Object principal);

    @Autowired UserEntityRepository userRepository;

    /**
     * Return the current user as a {@link UserEntity} to be inserted into a {@link org.springframework.data.annotation.CreatedBy} field.
     */
    public UserEntity getCurrentAuditor() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        UserEntity currentUser = principalToUser(authentication.getPrincipal());

        final UserEntity persistedUser = userRepository.findByDomainAndUserStoreAndUuidAndUid(
                currentUser.getDomain(),
                currentUser.getUserStore(),
                currentUser.getUuid(),
                currentUser.getUid()
        );

        if(persistedUser != null) {
            currentUser = persistedUser;
        } else {
            userRepository.save(currentUser);
        }

        return currentUser;
    }
}
