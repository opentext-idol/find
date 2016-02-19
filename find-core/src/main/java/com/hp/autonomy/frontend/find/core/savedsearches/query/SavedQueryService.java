/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches.query;

import com.hp.autonomy.frontend.find.core.savedsearches.AbstractSavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SavedQueryService extends AbstractSavedSearchService<SavedQuery> {
    private final SavedQueryRepository savedQueryRepository;

    @Autowired
    public SavedQueryService(final SavedQueryRepository savedQueryRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        super(savedQueryRepository, userEntityAuditorAware);
        this.savedQueryRepository = savedQueryRepository;
    }

    @Override
    protected Set<SavedQuery> getAllForUserId(final Long userId) {
        return savedQueryRepository.findByActiveTrueAndUser_UserId(userId);
    }
}
