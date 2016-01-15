/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class SavedSearchServiceImpl<I> implements SavedSearchService<I> {
    private final Set<SavedSearch<I>> savedSearches = new HashSet<>();
    private final AtomicLong idCount = new AtomicLong(2);

    protected SavedSearchServiceImpl(final I index) {
        final Set<FieldAndValue> parametricValues = new HashSet<>();
        parametricValues.add(new FieldAndValue("CATEGORY", "PERSON"));

        final Set<I> indexes = new HashSet<>();
        indexes.add(index);

        final SavedSearch<I> search = new SavedSearch.Builder<I>()
                .setId(1L)
                .setTitle("Star Wars")
                .setIndexes(indexes)
                .setParametricValues(parametricValues)
                .setQueryText("jedi OR sith")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setMinDate(DateTime.now().minus(Months.EIGHT))
                .build();

        savedSearches.add(search);
    }

    @Override
    public Set<SavedSearch<I>> getAll() {
        synchronized (savedSearches) {
            return savedSearches;
        }
    }

    @Override
    public SavedSearch<I> create(final SavedSearch<I> search) {
        synchronized (savedSearches) {
            final SavedSearch<I> newSearch = new SavedSearch.Builder<>(search)
                    .setId(idCount.getAndIncrement())
                    .build();

            savedSearches.add(newSearch);
            return newSearch;
        }
    }

    @Override
    public SavedSearch<I> update(final SavedSearch<I> search) {
        if (search.getId() == null || search.getTitle() == null) {
            // Placeholder only supports rename
            throw new IllegalArgumentException("ID and title required for update");
        }

        synchronized (savedSearches) {
            final SavedSearch<I> existingSearch = findById(search.getId());

            if (existingSearch == null) {
                throw new IllegalArgumentException("Search not found");
            }

            final SavedSearch<I> newSearch = new SavedSearch.Builder<>(existingSearch)
                    .setTitle(search.getTitle())
                    .build();

            savedSearches.remove(existingSearch);
            savedSearches.add(newSearch);
            return newSearch;
        }
    }

    @Override
    public void deleteById(final long id) {
        synchronized (savedSearches) {
            final SavedSearch<I> existingSearch = findById(id);

            if (existingSearch == null) {
                throw new IllegalArgumentException("Search not found");
            }

            savedSearches.remove(existingSearch);
        }
    }

    private SavedSearch<I> findById(final long id) {
        for (final SavedSearch<I> search : savedSearches) {
            if (search.getId().equals(id)) {
                return search;
            }
        }

        return null;
    }
}
