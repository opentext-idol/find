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

public class IdolQueryTermResult extends SharedResult {

    public IdolQueryTermResult(final String term, final ListView page) { super(term, page); }

    protected static Iterable<IdolQueryTermResult> idolResultsFor(final Iterable<String> queries, final QueryService service) {
        return new Iterable<IdolQueryTermResult>() {
            @Override
            public Iterator<IdolQueryTermResult> iterator() {
                final Iterator<String> queryIterator = queries.iterator();
                return new Iterator<IdolQueryTermResult>() {
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
                };
            }
        };
    }

    private static IdolQueryTermResult getTheResult(final String queryTerm, final QueryService service) {
        final Query query = new Query(queryTerm)
                .withFilter(new LanguageFilter(Language.ENGLISH));
        final ListView page = (ListView)service.search(query);
        return new IdolQueryTermResult(queryTerm, page);
    }

    public boolean errorWellExists() { return ((ListView)getPage()).errorContainerShown(); }
}
