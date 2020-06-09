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
package com.hp.autonomy.frontend.find.core.savedsearches;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractSavedSearchTest<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> {
    protected abstract SavedSearch.Builder<T, B> createBuilder();

    @Test
    public void toQueryTextWithNoConceptClusters() {
        final SavedSearch<T, B> search = createBuilder()
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("cats", true, -1)))
                .build();

        assertThat(search.toQueryText(), is("(cats)"));
    }

    @Test
    public void toQueryTextWithConceptClusters() {
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>(Arrays.asList(
                new ConceptClusterPhrase("\"fault line\"", true, 0),
                new ConceptClusterPhrase("\"impending doom\"", false, 0),
                new ConceptClusterPhrase("\"california\"", false, 0),
                new ConceptClusterPhrase("\"luke skywalker\"", true, 1),
                new ConceptClusterPhrase("raccoons", true, 2)
        ));

        final SavedSearch<T, B> search = createBuilder()
                .setConceptClusterPhrases(conceptClusterPhrases)
                .build();

        final String queryText = search.toQueryText();
        final List<String> concepts = new ArrayList<>(Arrays.asList(queryText.split(" AND ")));
        assertThat(concepts, hasSize(3));
        assertThat(concepts, hasItem("(\"luke skywalker\")"));
        assertThat(concepts, hasItem("(raccoons)"));

        concepts.remove("(\"luke skywalker\")");
        concepts.remove("(raccoons)");

        final List<String> clusterConcepts = Arrays.asList(concepts.get(0)
                                                                   .substring(1, concepts.get(0).length() - 1)
                                                                   .replace("\" \"", "\"\n\"")
                                                                   .split("\n"));
        assertThat(clusterConcepts, hasSize(3));
        assertThat(clusterConcepts.get(0), is("\"fault line\""));
        assertThat(clusterConcepts, hasItem("\"california\""));
        assertThat(clusterConcepts, hasItem("\"impending doom\""));
    }
}
