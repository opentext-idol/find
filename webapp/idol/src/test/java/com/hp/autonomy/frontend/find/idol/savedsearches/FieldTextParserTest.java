/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.savedsearches;

import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.savedsearches.*;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldTextParserTest {
    private static final Pattern AND_SEPARATOR = Pattern.compile("\\+AND\\+");
    private static final Pattern FIELD_TEXT_EXPRESSION_PREFIX = Pattern.compile("^(MATCH|RANGE|NRANGE)\\{");
    private static final Pattern FIELD_TEXT_EXPRESSION_SUFFIX = Pattern.compile("}:");
    @Mock
    private SavedSearch<?, ?> savedSearch;
    @Mock
    private ConfigService<IdolFindConfig> configService;
    @Mock
    private IdolFindConfig config;

    private FieldTextParser fieldTextParser;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
        when(config.getReferenceField()).thenReturn("CUSTOMREF");
        fieldTextParser = new FieldTextParserImpl(configService);
    }

    @Test
    public void toFieldTextWithNoFilters() {
        assertThat(fieldTextParser.toFieldText(savedSearch, true), is(""));
    }

    @Test
    public void toFieldTextWithOneFieldAndValue() {
        final FieldAndValue fieldAndValue = FieldAndValue.builder().field("SPECIES").value("cat").build();
        when(savedSearch.getParametricValues()).thenReturn(Collections.singleton(fieldAndValue));

        assertThat(fieldTextParser.toFieldText(savedSearch, true), is("MATCH{cat}:SPECIES"));
    }

    @Test
    public void toFieldTextWithOneNumericRange() {
        final NumericRangeRestriction range = NumericRangeRestriction.builder()
                .field("YEAR")
                .min(1066)
                .max(1485)
                .build();
        when(savedSearch.getNumericRangeRestrictions()).thenReturn(Collections.singleton(range));

        assertThat(fieldTextParser.toFieldText(savedSearch, true), is("NRANGE{1066,1485}:YEAR"));
    }

    @Test
    public void toFieldTextWithOneDateRange() {
        final DateRangeRestriction range = DateRangeRestriction.builder()
                .field("SOME_DATE")
                .min(ZonedDateTime.parse("2017-02-15T15:39:00Z"))
                .max(ZonedDateTime.parse("2017-02-15T15:40:00Z"))
                .build();
        when(savedSearch.getDateRangeRestrictions()).thenReturn(Collections.singleton(range));

        assertThat(fieldTextParser.toFieldText(savedSearch, true), is("RANGE{2017-02-15T15:39:00Z,2017-02-15T15:40:00Z}:SOME_DATE"));
    }

    @Test
    public void toFieldTextWithMultipleFieldsAndValuesAndRanges() {
        final FieldAndValue fieldAndValue1 = FieldAndValue.builder().field("SPECIES").value("cat").build();
        final FieldAndValue fieldAndValue2 = FieldAndValue.builder().field("SPECIES").value("dog").build();
        final FieldAndValue fieldAndValue3 = FieldAndValue.builder().field("COLOUR").value("white").build();

        final NumericRangeRestriction numericRange = NumericRangeRestriction.builder().field("YEAR")
                .min(1066)
                .max(1485)
                .build();
        final DateRangeRestriction dateRange = DateRangeRestriction.builder()
                .field("DATE")
                .min(ZonedDateTime.parse("2017-02-15T15:39:00Z"))
                .max(ZonedDateTime.parse("2017-02-15T15:40:00Z"))
                .build();

        when(savedSearch.getParametricValues()).thenReturn(ImmutableSet.of(fieldAndValue1, fieldAndValue2, fieldAndValue3));
        when(savedSearch.getNumericRangeRestrictions()).thenReturn(Collections.singleton(numericRange));
        when(savedSearch.getDateRangeRestrictions()).thenReturn(Collections.singleton(dateRange));

        final String fieldText = fieldTextParser.toFieldText(savedSearch, true);

        final String[] expressions = AND_SEPARATOR.split(fieldText);
        assertThat(expressions, arrayWithSize(4));

        final Map<String, String[]> fieldToValues = new HashMap<>();

        for (final String expression : expressions) {
            // Expression should have the form (e.g) MATCH{<value1>,<value2>,...}:<field>
            final String[] splitExpression = FIELD_TEXT_EXPRESSION_SUFFIX.split(FIELD_TEXT_EXPRESSION_PREFIX.matcher(expression).replaceFirst(""));
            assertThat(splitExpression, arrayWithSize(2));
            fieldToValues.put(splitExpression[1], splitExpression[0].split(","));
        }

        assertThat(fieldToValues, hasEntry(is("COLOUR"), arrayContainingInAnyOrder("white")));
        assertThat(fieldToValues, hasEntry(is("SPECIES"), arrayContainingInAnyOrder("dog", "cat")));
        assertThat(fieldToValues, hasEntry(is("YEAR"), arrayContaining("1066", "1485")));
        assertThat(fieldToValues, hasEntry(is("DATE"), arrayContaining("2017-02-15T15:39:00Z", "2017-02-15T15:40:00Z")));
    }

    @Test
    public void toFieldTextWithEmptyDocumentWhitelist() {
        when(savedSearch.getDocumentSelectionIsWhitelist()).thenReturn(true);
        assertThat(fieldTextParser.toFieldText(savedSearch, true), is("NOTWILD{*}:CUSTOMREF"));
    }

    @Test
    public void toFieldTextWithDocumentWhitelist() {
        when(savedSearch.getDocumentSelectionIsWhitelist()).thenReturn(true);
        when(savedSearch.getDocumentSelection()).thenReturn(new HashSet<>(Arrays.asList(
            new DocumentSelection("ref1"),
            new DocumentSelection("ref,2")
        )));
        assertThat(fieldTextParser.toFieldText(savedSearch, true), isOneOf(
            "MATCH{ref1,ref%2C2}:CUSTOMREF",
            "MATCH{ref%2C2,ref1}:CUSTOMREF"
        ));
    }

    @Test
    public void toFieldTextWithDocumentBlacklist() {
        when(savedSearch.getDocumentSelection()).thenReturn(new HashSet<>(Arrays.asList(
            new DocumentSelection("ref1"),
            new DocumentSelection("ref,2")
        )));
        assertThat(fieldTextParser.toFieldText(savedSearch, true), isOneOf(
            "NOT+MATCH{ref1,ref%2C2}:CUSTOMREF",
            "NOT+MATCH{ref%2C2,ref1}:CUSTOMREF"
        ));
    }

    @Test
    public void toFieldTextWithIgnoredDocumentBlacklist() {
        when(savedSearch.getDocumentSelection()).thenReturn(new HashSet<>(Arrays.asList(
            new DocumentSelection("ref1"),
            new DocumentSelection("ref,2")
        )));
        assertThat(fieldTextParser.toFieldText(savedSearch, false), is(""));
    }

}
