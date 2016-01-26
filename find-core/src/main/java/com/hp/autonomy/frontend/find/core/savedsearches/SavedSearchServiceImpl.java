/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class SavedSearchServiceImpl implements SavedSearchService {
    private final Set<SavedSearch> savedSearches = new HashSet<>();
    private final AtomicLong idCount = new AtomicLong(2);

    protected SavedSearchServiceImpl(final NameAndDomain index) {
        final Set<FieldAndValue> parametricValues = new HashSet<>();
        parametricValues.add(new FieldAndValue("WIKIPEDIA_CATEGORY", "存命人物"));

        final Set<NameAndDomain> indexes = new HashSet<>();
        indexes.add(index);

        final SavedSearch search = new SavedSearch.Builder()
                .setId(1L)
                .setTitle("Cats..?")
                .setIndexes(indexes)
                .setRelatedConcepts(Collections.singleton("シグマ セブン"))
                .setParametricValues(parametricValues)
                .setInputText("cat")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setMinDate(DateTime.now().minus(Months.EIGHT))
                .build();

        savedSearches.add(search);
    }

    @Override
    public Set<SavedSearch> getAll() {
        synchronized (savedSearches) {
            return savedSearches;
        }
    }

    @Override
    public SavedSearch create(final SavedSearch search) {
        synchronized (savedSearches) {
            final SavedSearch newSearch = new SavedSearch.Builder(search)
                    .setId(idCount.getAndIncrement())
                    .build();

            savedSearches.add(newSearch);
            return newSearch;
        }
    }

    @Override
    public SavedSearch update(final SavedSearch search) {
        if (search.getId() == null || search.getTitle() == null) {
            // Placeholder only supports rename
            throw new IllegalArgumentException("ID and title required for update");
        }

        synchronized (savedSearches) {
            final SavedSearch existingSearch = findById(search.getId());

            if (existingSearch == null) {
                throw new IllegalArgumentException("Search not found");
            }

            final SavedSearch newSearch = new SavedSearch.Builder(existingSearch)
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
            final SavedSearch existingSearch = findById(id);

            if (existingSearch == null) {
                throw new IllegalArgumentException("Search not found");
            }

            savedSearches.remove(existingSearch);
        }
    }

    private SavedSearch findById(final long id) {
        for (final SavedSearch search : savedSearches) {
            if (search.getId().equals(id)) {
                return search;
            }
        }

        return null;
    }
}
