/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.queryHelper;

import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;
import com.autonomy.abc.shared.SharedResult;

import java.util.Iterator;

class IdolQueryTermResult extends SharedResult<ListView> {
    private IdolQueryTermResult(final String term, final ListView page) {
        super(term, page);
    }

    static Iterable<IdolQueryTermResult> idolResultsFor(final Iterable<String> queries, final QueryService<ListView> service) {
        return () -> {
            final Iterator<String> queryIterator = queries.iterator();
            return new IdolQueryTermResultIterator(queryIterator, service);
        };
    }

    private static IdolQueryTermResult getTheResult(final String queryTerm, final QueryService<ListView> service) {
        final Query query = new Query(queryTerm)
                .withFilter(new LanguageFilter(Language.ENGLISH));
        final ListView page = service.search(query);

        return new IdolQueryTermResult(queryTerm, page);
    }

    boolean errorWellExists() {
        return getPage().errorContainerShown();
    }

    private static class IdolQueryTermResultIterator implements Iterator<IdolQueryTermResult> {
        private final Iterator<String> queryIterator;
        private final QueryService<ListView> service;

        IdolQueryTermResultIterator(final Iterator<String> queryIterator, final QueryService<ListView> service) {
            this.queryIterator = queryIterator;
            this.service = service;
        }

        @Override
        public boolean hasNext() {
            return queryIterator.hasNext();
        }

        @Override
        public IdolQueryTermResult next() {
            return getTheResult(queryIterator.next(), service);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
