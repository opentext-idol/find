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

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.springframework.data.domain.AuditorAware;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public abstract class AbstractSavedSearchService<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> implements SavedSearchService<T, B> {
    private final SavedSearchRepository<T, B> crudRepository;
    private final SharedToUserRepository sharedToUserRepository;
    private final SharedToEveryoneRepository sharedToEveryoneRepository;
    private final AuditorAware<UserEntity> userEntityAuditorAware;
    private final TagNameFactory tagNameFactory;
    private final Class<T> type;

    protected AbstractSavedSearchService(final SavedSearchRepository<T, B> crudRepository,
                                         final SharedToUserRepository sharedToUserRepository,
                                         final SharedToEveryoneRepository sharedToEveryoneRepository,
                                         final AuditorAware<UserEntity> userEntityAuditorAware,
                                         final TagNameFactory tagNameFactory,
                                         final Class<T> type) {
        this.crudRepository = crudRepository;
        this.sharedToUserRepository = sharedToUserRepository;
        this.sharedToEveryoneRepository = sharedToEveryoneRepository;
        this.userEntityAuditorAware = userEntityAuditorAware;
        this.tagNameFactory = tagNameFactory;
        this.type = type;
    }

    @Override
    public Set<T> getOwned() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();

        return augmentOutputWithDisplayNames(crudRepository.findByActiveTrueAndUser_UserId(userId));
    }

    @Override
    public Set<T> getShared() {
        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final Set<SharedToUser> permissions = sharedToUserRepository.findByUserId(userId, type);

        final Set<Long> uniqueSavedSearchIds = new HashSet<Long>();

        final Set<T> userShared = permissions.stream()
                .map(sharedToUser -> {
                    uniqueSavedSearchIds.add(sharedToUser.getSavedSearch().getId());
                    return type.cast(sharedToUser.getSavedSearch().toBuilder()
                            .setCanEdit(sharedToUser.getCanEdit())
                            .build());
                })
                .collect(toSet());

        final LinkedHashSet<T> shared = new LinkedHashSet<>(userShared);

        for(SharedToEveryone globalShare : sharedToEveryoneRepository.findActiveByType(type)) {
            final SavedSearch<?, ?> savedSearch = globalShare.getSavedSearch();
            if (!userId.equals(savedSearch.getUser().getUserId()) && !uniqueSavedSearchIds.contains(savedSearch.getId())) {
                shared.add(type.cast(savedSearch));
            }
        }

        return augmentOutputWithDisplayNames(shared);
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
        final T search = crudRepository.findByActiveTrueAndId(id);

        final Long userId = userEntityAuditorAware.getCurrentAuditor().getUserId();

        if (!Objects.equals(search.getUser().getUserId(), userId)) {
            search.setCanEdit(isUnownedSearchEditable(search, userId));
        }

        return search;
    }

    protected boolean isUnownedSearchEditable(final T search, final Long userId) {
        final SharedToUser share = sharedToUserRepository.findOne(new SharedToUserPK(search.getId(), userId));
        return share != null && Boolean.TRUE.equals(share.getCanEdit());
    }

    private T getSearch(final long id) throws IllegalArgumentException {
        final Long currentUserId = userEntityAuditorAware.getCurrentAuditor().getUserId();
        final T savedSearch = crudRepository.findByActiveTrueAndId(id);

        if(null != savedSearch) {
            if (savedSearch.getUser().getUserId().equals(currentUserId)
                || sharedToUserRepository.findOne(new SharedToUserPK(id, currentUserId)) != null
                || sharedToEveryoneRepository.findOne(new SharedToEveryonePK(id)) != null
            ) {
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
