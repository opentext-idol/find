/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import org.springframework.data.domain.AuditorAware;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

        final Collection<T> results = new HashSet<>();
        results.addAll(crudRepository.findByActiveTrueAndUser_UserId(userId));

        // In addition to saved searches where the user is the owner, also return saved searches that the user has
        // been given permission to view.
        final Set<SharedToUser> permissions = sharedToUserRepository.findByUserId(userId, type);
        // Set the canEdit field on the saved search from sharedToUser. By default saved search canEdit is true.
        results.addAll(permissions.stream()
                .map(sharedToUser -> type.cast(sharedToUser.getSavedSearch().toBuilder()
                        .setCanEdit(sharedToUser.getCanEdit())
                        .build()))
                .collect(toSet()));

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
        return crudRepository.findByActiveTrueAndId(id);
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
                .setNumericRangeRestrictions(Optional.ofNullable(result.getNumericRangeRestrictions())
                        .map(numericRanges -> numericRanges.stream()
                                .map(numericRange -> numericRange.toBuilder()
                                        .displayName(tagNameFactory.buildTagName(numericRange.getField()).getDisplayName())
                                        .build())
                                .collect(toSet()))
                        .orElse(Collections.emptySet()))
                .setDateRangeRestrictions(Optional.ofNullable(result.getDateRangeRestrictions())
                        .map(dateRanges -> dateRanges.stream()
                                .map(dateRange -> dateRange.toBuilder()
                                        .displayName(tagNameFactory.buildTagName(dateRange.getField()).getDisplayName())
                                        .build())
                                .collect(toSet()))
                        .orElse(Collections.emptySet()))
                .build();
    }
}
