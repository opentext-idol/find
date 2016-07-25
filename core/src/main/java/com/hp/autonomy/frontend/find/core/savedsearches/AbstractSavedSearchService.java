/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.springframework.data.domain.AuditorAware;

import java.util.Set;

public abstract class AbstractSavedSearchService<T extends SavedSearch<T>> implements SavedSearchService<T> {
    private final SavedSearchRepository<T> crudRepository;
    private final AuditorAware<UserEntity> userEntityAuditorAware;

    protected AbstractSavedSearchService(final SavedSearchRepository<T> crudRepository, final AuditorAware<UserEntity> userEntityAuditorAware) {
        this.crudRepository = crudRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
    }

    @Override
    public Set<T> getAll() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        return crudRepository.findByActiveTrueAndUser_UserId(userId);
    }

    @Override
    public T get(final long id) {
        return getSearch(id);
    }

    @Override
    public T create(final T search) {
        return crudRepository.save(search);
    }

    @Override
    public T update(final T search) {
        final T savedQuery = getSearch(search.getId());
        savedQuery.merge(search);
        return crudRepository.save(savedQuery);
    }

    @Override
    public void deleteById(final long id) {
        final T savedQuery = getSearch(id);
        savedQuery.setActive(false);
        crudRepository.save(savedQuery);

    }

    private T getSearch(long id) throws IllegalArgumentException {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final T byIdAndUser_userId = crudRepository.findByActiveTrueAndIdAndUser_UserId(id, userId);

        if (null != byIdAndUser_userId) {
            return byIdAndUser_userId;
        }
        else {
            throw new IllegalArgumentException("Saved search not found");
        }
    }
}
