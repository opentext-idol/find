/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractSavedSearchTest<T extends SavedSearch<T>> {
    protected abstract SavedSearch.Builder<T> createBuilder();

    @Test
    public void toQueryTextWithNoConceptClusters() {
        final SavedSearch<T> search = createBuilder()
                .setQueryText("cats")
                .build();

        assertThat(search.toQueryText(), is("cats"));
    }

    @Test
    public void toQueryTextWithConceptClusters() {
        final Set<ConceptClusterPhrase> conceptClusterPhrases = new HashSet<>();

        final ConceptClusterPhrase countyClusterPhrase = new ConceptClusterPhrase("county", true, 0);
        final ConceptClusterPhrase californiaClusterPhrase = new ConceptClusterPhrase("california", false, 0);
        conceptClusterPhrases.add(countyClusterPhrase);
        conceptClusterPhrases.add(californiaClusterPhrase);

        final ConceptClusterPhrase lukeClusterPhrase = new ConceptClusterPhrase("luke skywalker", true, 1);
        conceptClusterPhrases.add(lukeClusterPhrase);

        final SavedSearch<T> search = createBuilder()
                .setQueryText("orange jedi")
                .setConceptClusterPhrases(conceptClusterPhrases)
                .build();

        final String queryText = search.toQueryText();
        assertThat(queryText, containsString("(orange jedi)"));
        assertThat(queryText, containsString("\"luke skywalker\""));
        assertThat(queryText, containsString("\"california\""));
        assertThat(queryText, containsString("\"county\""));
    }

    @Test
    public void toFieldTextWithNoParametricValues() {
        final SavedSearch<T> search = createBuilder()
                .setParametricValues(Collections.<FieldAndValue>emptySet())
                .build();

        assertThat(search.toFieldText(), is(""));
    }

    @Test
    public void toFieldTextWithOneFieldAndValue() {
        final FieldAndValue fieldAndValue = new FieldAndValue("SPECIES", "cat");

        final SavedSearch<T> search = createBuilder()
                .setParametricValues(Collections.singleton(fieldAndValue))
                .build();

        assertThat(search.toFieldText(), is("MATCH{cat}:SPECIES"));
    }

    @Test
    public void toFieldTextWithMultipleFieldsAndValues() {
        final Set<FieldAndValue> parametricValues = new HashSet<>();

        final FieldAndValue fieldAndValue1 = new FieldAndValue("SPECIES", "cat");
        parametricValues.add(fieldAndValue1);

        final FieldAndValue fieldAndValue2 = new FieldAndValue("SPECIES", "dog");
        parametricValues.add(fieldAndValue2);

        final FieldAndValue fieldAndValue3 = new FieldAndValue("COLOUR", "white");
        parametricValues.add(fieldAndValue3);

        final SavedSearch<T> search = createBuilder()
                .setParametricValues(parametricValues)
                .build();

        final String fieldText = search.toFieldText();

        final String[] expressions = fieldText.split("\\+AND\\+");
        assertThat(expressions, arrayWithSize(2));

        final Map<String, String[]> fieldToValues = new HashMap<>();

        for (final String expression : expressions) {
            // Expression should have the form MATCH{<value1>,<value2>,...}:<field>
            final String[] splitExpression = expression.replaceFirst("^MATCH\\{", "").split("\\}:");
            assertThat(splitExpression, arrayWithSize(2));
            fieldToValues.put(splitExpression[1], splitExpression[0].split(","));
        }

        assertThat(fieldToValues, hasEntry(is("COLOUR"), arrayContainingInAnyOrder("white")));
        assertThat(fieldToValues, hasEntry(is("SPECIES"), arrayContainingInAnyOrder("dog", "cat")));
    }
}
