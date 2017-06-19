/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import org.springframework.data.domain.AuditorAware;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public abstract class AbstractSavedSearchService<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> implements SavedSearchService<T, B> {
    private final SavedSearchRepository<T, B> crudRepository;
    private final SharedToUserRepository sharedToUserRepository;
    private final AuditorAware<UserEntity> userEntityAuditorAware;
    private final TagNameFactory tagNameFactory;
    private final Class<T> type;

    protected AbstractSavedSearchService(final SavedSearchRepository<T, B> crudRepository,
                                         final SharedToUserRepository sharedToUserRepository,
                                         final AuditorAware<UserEntity> userEntityAuditorAware,
                                         final TagNameFactory tagNameFactory,
                                         final Class<T> type) {
        this.crudRepository = crudRepository;
        this.sharedToUserRepository = sharedToUserRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
        this.tagNameFactory = tagNameFactory;
        this.type = type;
    }

    @Override
    public Set<T> getAll() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();

        return augmentOutputWithDisplayNames(crudRepository.findByActiveTrueAndUser_UserId(userId));
    }

    @Override
    public Set<T> getShared() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final Set<SharedToUser> permissions = sharedToUserRepository.findByUserId(userId, type);

        return augmentOutputWithDisplayNames(permissions.stream()
                .map(sharedToUser -> type.cast(sharedToUser.getSavedSearch().toBuilder()
                        .setCanEdit(sharedToUser.getCanEdit())
                        .build()))
                .collect(toSet()));
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
    public T updateShared(final T search) {
        final T savedQuery = getSearch(search.getId());

        if(savedQuery.isCanEdit()) {
            savedQuery.merge(search);
            final T result = crudRepository.save(savedQuery);
            return augmentOutputWithDisplayNames(result);
        } else {
            throw new IllegalArgumentException("User does not have permission to edit the search");
        }
    }

    @Override
    public void deleteById(final long id) {
        final T savedQuery = getSearch(id);
        savedQuery.setActive(false);
        crudRepository.save(savedQuery);

    }

    @Override
    public T getDashboardSearch(final long id) {
        return crudRepository.findByActiveTrueAndId(id);
    }

    private T getSearch(final long id) throws IllegalArgumentException {
        final Long currentUserId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final T savedSearch = crudRepository.findByActiveTrueAndId(id);

        if(null != savedSearch) {
            if (savedSearch.getUser().getUserId().equals(currentUserId) || sharedToUserRepository.findOne(new SharedToUserPK(id, currentUserId)) != null) {
                return savedSearch;
            } else {
                throw new IllegalArgumentException("User has no permissions to edit this saved search");
            }
        } else {
            throw new IllegalArgumentException("Saved search not found");
        }
    }

    private Set<T> augmentOutputWithDisplayNames(final Collection<T> results) {
        return results.stream()
                .map(this::augmentOutputWithDisplayNames)
                .collect(toSet());
    }

    private T augmentOutputWithDisplayNames(final T result) {
        return result.toBuilder()
                .setParametricValues(Optional.ofNullable(result.getParametricValues())
                        .map(parametricValues -> parametricValues
                                .stream()
                                .map(parametricValue -> parametricValue.toBuilder()
                                        .displayName(tagNameFactory.buildTagName(parametricValue.getField()).getDisplayName())
                                        .displayValue(tagNameFactory.getTagDisplayValue(parametricValue.getField(), parametricValue.getValue()))
                                        .build())
                                .collect(toSet()))
                        .orElse(Collections.emptySet()))
                .setParametricRanges(Optional.ofNullable(result.getParametricRanges())
                        .map(parametricRanges -> parametricRanges.stream()
                                .map(parametricRange -> parametricRange.toBuilder()
                                        .displayName(tagNameFactory.buildTagName(parametricRange.getField()).getDisplayName())
                                        .build())
                                .collect(toSet()))
                        .orElse(Collections.emptySet()))
                .build();
    }
}
