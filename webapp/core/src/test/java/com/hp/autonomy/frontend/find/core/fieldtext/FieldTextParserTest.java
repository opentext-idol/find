/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fieldtext;

import com.hp.autonomy.frontend.find.core.fields.FieldAndValue;
import com.hp.autonomy.frontend.find.core.fields.ParametricRange;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.searchcomponents.core.fields.FieldsMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldTextParserTest {
    private static final Pattern AND_SEPARATOR = Pattern.compile("\\+AND\\+");
    private static final Pattern FIELD_TEXT_EXPRESSION_PREFIX = Pattern.compile("^(MATCH|RANGE|NRANGE|EXISTS)\\{");
    private static final Pattern FIELD_TEXT_EXPRESSION_SUFFIX = Pattern.compile("\\}:");
    @Mock
    private SavedSearch<?> savedSearch;

    @Mock
    private FieldsMapper fieldsMapper;

    private FieldTextParser fieldTextParser;

    @Before
    public void setUp() {
        when(fieldsMapper.restoreFieldValue(anyString(), anyString())).thenAnswer(invocation -> Collections.singleton(invocation.getArgumentAt(1, String.class)));
        fieldTextParser = new FieldTextParserImpl(fieldsMapper);
    }

    @Test
    public void toFieldTextWithNoParametricValuesOrRanges() {
        assertThat(fieldTextParser.toFieldText(null, null, null), is(""));
    }

    @Test
    public void toFieldTextWithOneFieldAndValue() {
        final FieldAndValue fieldAndValue = new FieldAndValue("DOCUMENT/SPECIES", "cat");
        assertThat(fieldTextParser.toFieldText(Collections.singleton(fieldAndValue), null, null), is("MATCH{cat}:DOCUMENT/SPECIES"));
    }

    @Test
    public void toFieldTextWithOneRange() {
        final ParametricRange range = new ParametricRange("DOCUMENT/YEAR", 1066, 1485, ParametricRange.Type.Numeric);

        assertThat(fieldTextParser.toFieldText(null, Collections.singleton(range), null), is("NRANGE{1066,1485}:DOCUMENT/YEAR"));
    }

    @Test
    public void toFieldTextWithMultipleFieldsAndValuesAndRanges() {
        final Set<FieldAndValue> fieldAndValues = getFieldAndValues();

        final Set<ParametricRange> parametricRanges = getParametricRanges();

        final String fieldText = fieldTextParser.toFieldText(fieldAndValues, parametricRanges, null);

        final String[] expressions = AND_SEPARATOR.split(fieldText);
        assertThat(expressions, arrayWithSize(4));

        final Map<String, String[]> fieldToValues = new HashMap<>();

        for (final String expression : expressions) {
            // Expression should have the form (e.g) MATCH{<value1>,<value2>,...}:<field>
            final String[] splitExpression = FIELD_TEXT_EXPRESSION_SUFFIX.split(FIELD_TEXT_EXPRESSION_PREFIX.matcher(expression).replaceFirst(""));
            assertThat(splitExpression, arrayWithSize(2));
            fieldToValues.put(splitExpression[1], splitExpression[0].split(","));
        }

        assertThat(fieldToValues, hasEntry(is("DOCUMENT/COLOUR"), arrayContainingInAnyOrder("white")));
        assertThat(fieldToValues, hasEntry(is("DOCUMENT/SPECIES"), arrayContainingInAnyOrder("dog", "cat")));
        assertThat(fieldToValues, hasEntry(is("DOCUMENT/YEAR"), arrayContaining("1066", "1485")));
        assertThat(fieldToValues, hasEntry(is("DOCUMENT/DATE"), arrayContaining("123456789e", "123456791e")));
    }

    private Set<FieldAndValue> getFieldAndValues() {
        final Set<FieldAndValue> fieldAndValues = new HashSet<>(3);

        fieldAndValues.add(new FieldAndValue("DOCUMENT/SPECIES", "cat"));
        fieldAndValues.add(new FieldAndValue("DOCUMENT/SPECIES", "dog"));
        fieldAndValues.add(new FieldAndValue("DOCUMENT/COLOUR", "white"));
        return fieldAndValues;
    }

    private Set<ParametricRange> getParametricRanges() {
        final Set<ParametricRange> parametricRanges = new HashSet<>(2);

        parametricRanges.add(new ParametricRange("DOCUMENT/YEAR", 1066, 1485, ParametricRange.Type.Numeric));
        parametricRanges.add(new ParametricRange("DOCUMENT/DATE", 123456789, 123456791, ParametricRange.Type.Date));
        return parametricRanges;
    }
}
