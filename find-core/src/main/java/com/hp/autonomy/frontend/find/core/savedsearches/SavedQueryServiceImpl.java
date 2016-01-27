/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class SavedQueryServiceImpl implements SavedQueryService {

    private SavedQueryRepository savedQueryRepository;
    private AuditorAware<UserEntity> userEntityAuditorAware;

    @Autowired
    public SavedQueryServiceImpl(final SavedQueryRepository savedQueryRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        this.savedQueryRepository = savedQueryRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
    }

    @Override
    public Set<SavedQuery> getAll() {
        final Integer userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        return savedQueryRepository.findByActiveTrueAndUser_UserId(userId);
    }

    @Override
    public SavedQuery create(final SavedQuery query) {
        return savedQueryRepository.save(query);
    }

    @Override
    public SavedQuery update(final SavedQuery query) {
        return savedQueryRepository.save(query);
    }

    @Override
    public void deleteById(final int id) {
        final SavedQuery savedQuery = savedQueryRepository.findOne(id);
        savedQuery.setActive(false);
        savedQueryRepository.save(savedQuery);
    }
}
