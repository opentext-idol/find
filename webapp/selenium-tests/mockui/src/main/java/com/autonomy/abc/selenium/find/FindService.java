/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.find.results.SimilarDocumentsView;
import com.autonomy.abc.selenium.query.AggregateQueryFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindService implements QueryService<ListView> {
    private final FindElementFactory elementFactory;
    private final FindPage findPage;

    public FindService(final FindApplication<?> find) {
        elementFactory = find.elementFactory();
        findPage = elementFactory.getFindPage();
    }

    @Override
    public ListView search(final String query) {
        return search(new Query(query));
    }

    @Override
    public ListView search(final Query query) {
        submitSearch(query.getTerm());
        elementFactory.getFilterPanel().waitForIndexes();
        findPage.filterBy(new AggregateQueryFilter(query.getFilters()));
        LoggerFactory.getLogger(getClass()).info("'search' expects you to already be on the listview, if it fails " +
                "try 'searchAnyView'.");
        return elementFactory.getListView();
    }

    public void searchAnyView(final String term) {
        searchAnyView(new Query(term));
    }

    public void searchAnyView(final Query query) {
        submitSearch(query.getTerm());
        elementFactory.getFilterPanel().waitForIndexes();
        findPage.filterBy(new AggregateQueryFilter(query.getFilters()));
    }

    protected void submitSearch(final String term) {
        elementFactory.getSearchBox().setValue(term);
        Waits.loadOrFadeWait();
        elementFactory.getSearchBox().submit();
    }

    public SimilarDocumentsView goToSimilarDocuments(final int resultNumber) {
        final ListView resultsPage = elementFactory.getListView();
        resultsPage.getResult(resultNumber).similarDocuments().click();
        final SimilarDocumentsView similarDocuments = elementFactory.getSimilarDocumentsView();
        similarDocuments.waitForLoad();
        return similarDocuments;
    }

    public String termWithBetween1And30Results(final List<String> candidates) {
        for (final String term : candidates) {
            final ListView results = search(term);
            findPage.ensureTermNotAutoCorrected();
            findPage.waitForLoad();
            final int resultsNum = results.getTotalResultsNum();
            elementFactory.getConceptsPanel().removeAllConcepts();

            if (resultsNum > 0 && resultsNum <= 30) {
                candidates.subList(0, candidates.indexOf(term) + 1).clear();
                return term;
            }
        }
        return "";
    }

    public ImmutablePair<String, String> getPairOfTermsThatDoNotShareResults() {
        final List<ImmutablePair<String, String>> potentialTerms = Arrays.asList(
                new ImmutablePair<>("\"polar bear\"", "\"opposable thumbs\""),
                new ImmutablePair<>("\"upshot\"", "\"space invaders\""),
                new ImmutablePair<>("\"animalistic\"", "\"freefall\""));

        for (final ImmutablePair<String, String> pair : potentialTerms) {
            final Set<String> results1 = searchAndGetResults(pair.getLeft());
            final Set<String> results2 = searchAndGetResults(pair.getRight());

            if (!results1.isEmpty() && !results2.isEmpty()) {
                results1.retainAll(results2);
                if (results1.isEmpty()) {
                    return pair;
                }
            }
        }
        return new ImmutablePair<>("", "");
    }

    private Set<String> searchAndGetResults(final String term) {
        search(term);

        final ListView listView = elementFactory.getListView();
        listView.waitForResultsToLoad();

        final Set<String> results = new HashSet<>(listView.getResultTitles());
        elementFactory.getConceptsPanel().removeAllConcepts();
        return results;
    }

    /**
     * @param query The query text
     * @return The URL of the search page for the given query text, relative to the application context path
     */
    public String getQueryUrl(final String query) {
        try {
            return "public/search/query/" + URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e);
        }
    }
}
