/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import org.springframework.data.domain.AuditorAware;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSavedSearchService<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> implements SavedSearchService<T, B> {
    private final SavedSearchRepository<T, B> crudRepository;
    private final AuditorAware<UserEntity> userEntityAuditorAware;
    private final TagNameFactory tagNameFactory;

    protected AbstractSavedSearchService(final SavedSearchRepository<T, B> crudRepository,
                                         final AuditorAware<UserEntity> userEntityAuditorAware,
                                         final TagNameFactory tagNameFactory) {
        this.crudRepository = crudRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
        this.tagNameFactory = tagNameFactory;
    }

    @Override
    public Set<T> getAll() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final Set<T> results = crudRepository.findByActiveTrueAndUser_UserId(userId);
        return augmentOutputWithDisplayNames(results);
    }

    @Override
    public T get(final long id) {
        final T result = getSearch(id);
        return augmentOutputWithDisplayNames(result);
    }

    @Override
    public T create(final T search) {
        final T result = crudRepository.save(search);
        return augmentOutputWithDisplayNames(result);
    }

    @Override
    public T update(final T search) {
        final T savedQuery = getSearch(search.getId());
        savedQuery.merge(search);
        final T result = crudRepository.save(savedQuery);
        return augmentOutputWithDisplayNames(result);
    }

    @Override
    public void deleteById(final long id) {
        final T savedQuery = getSearch(id);
        savedQuery.setActive(false);
        crudRepository.save(savedQuery);

    }

    @Override
    public T getDashboardSearch(final long id) {
        return crudRepository.findOne(id);
    }

    private T getSearch(final long id) throws IllegalArgumentException {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final T byIdAndUser_userId = crudRepository.findByActiveTrueAndIdAndUser_UserId(id, userId);

        if (null != byIdAndUser_userId) {
            return byIdAndUser_userId;
        } else {
            throw new IllegalArgumentException("Saved search not found");
        }
    }

    private Set<T> augmentOutputWithDisplayNames(final Collection<T> results) {
        return results.stream()
                .map(this::augmentOutputWithDisplayNames)
                .collect(Collectors.toSet());
    }

    private T augmentOutputWithDisplayNames(final T result) {
        return result.toBuilder()
                .setParametricValues(result.getParametricValues()
                        .stream()
                        .map(parametricValue -> parametricValue.toBuilder()
                                .displayName(tagNameFactory.buildTagName(parametricValue.getField()).getDisplayName())
                                .displayValue(tagNameFactory.getTagDisplayValue(parametricValue.getField(), parametricValue.getValue()))
                                .build())
                        .collect(Collectors.toSet()))
                .setParametricRanges(result.getParametricRanges()
                        .stream()
                        .map(parametricRange -> parametricRange.toBuilder()
                                .displayName(tagNameFactory.buildTagName(parametricRange.getField()).getDisplayName())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
