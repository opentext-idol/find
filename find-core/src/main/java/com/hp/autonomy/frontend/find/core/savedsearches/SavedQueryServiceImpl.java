/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SavedQueryServiceImpl<I> implements SavedQueryService<I> {
    private final Set<SavedQuery<I>> savedQueries = new HashSet<>();
    private final AtomicInteger idCount = new AtomicInteger(2);

    protected SavedQueryServiceImpl(final I index) {
        final Set<FieldAndValue> parametricValues = new HashSet<>();
        parametricValues.add(new FieldAndValue("CATEGORY", "PERSON"));

        final Set<I> indexes = new HashSet<>();
        indexes.add(index);

        final SavedQuery<I> query = new SavedQuery.Builder<I>()
                .setId(1)
                .setTitle("Star Wars")
                .setIndexes(indexes)
                .setParametricValues(parametricValues)
                .setQueryText("jedi OR sith")
                .setDateCreated(DateTime.now())
                .setDateModified(DateTime.now())
                .setMinDate(DateTime.now().minus(Months.EIGHT))
                .build();

        savedQueries.add(query);
    }

    @Override
    public Set<SavedQuery<I>> getAll() {
        synchronized (savedQueries) {
            return savedQueries;
        }
    }

    @Override
    public SavedQuery<I> create(final SavedQuery<I> query) {
        synchronized (savedQueries) {
            final SavedQuery<I> newQuery = new SavedQuery.Builder<>(query)
                    .setId(idCount.getAndIncrement())
                    .build();

            savedQueries.add(newQuery);
            return newQuery;
        }
    }

    @Override
    public SavedQuery<I> update(final SavedQuery<I> query) {
        if (query.getId() == null || query.getTitle() == null) {
            // Placeholder only supports rename
            throw new IllegalArgumentException("ID and title required for update");
        }

        synchronized (savedQueries) {
            final SavedQuery<I> existingQuery = findById(query.getId());

            if (existingQuery == null) {
                throw new IllegalArgumentException("Query not found");
            }

            final SavedQuery<I> newQuery = new SavedQuery.Builder<>(existingQuery)
                    .setTitle(query.getTitle())
                    .build();

            savedQueries.remove(existingQuery);
            savedQueries.add(newQuery);
            return newQuery;
        }
    }

    @Override
    public void deleteById(final int id) {
        synchronized (savedQueries) {
            final SavedQuery<I> existingQuery = findById(id);

            if (existingQuery == null) {
                throw new IllegalArgumentException("Query not found");
            }

            savedQueries.remove(existingQuery);
        }
    }

    private SavedQuery<I> findById(final int id) {
        for (final SavedQuery<I> query : savedQueries) {
            if (query.getId().equals(id)) {
                return query;
            }
        }

        return null;
    }
}
