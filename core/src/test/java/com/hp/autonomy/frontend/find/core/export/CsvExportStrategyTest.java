/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsvExportStrategyTest {
    @Mock
    private ConfigService<? extends HavenSearchCapable> configService;
    @Mock
    private HavenSearchCapable config;

    private CsvExportStrategy csvExportStrategy;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
        when(config.getFieldsInfo()).thenReturn(new FieldsInfo.Builder()
                .populateResponseMap("authors", new FieldInfo<String>("authors", Arrays.asList("AUTHOR", "author"), FieldType.STRING))
                .populateResponseMap("categories", new FieldInfo<String>("categories", Arrays.asList("CATEGORY", "category"), FieldType.STRING))
                .build());

        csvExportStrategy = new CsvExportStrategy(configService);
    }

    @Test
    public void writeHeader() {
        assertThat(csvExportStrategy.writeHeader(), is(true));
    }

    @Test
    public void getFieldNames() {
        assertThat(csvExportStrategy.getFieldNames(new MetadataNode[]{mock(MetadataNode.class)}), hasSize(3));
    }

    @Test
    public void getConfiguredFields() {
        assertThat(csvExportStrategy.getConfiguredFields().keySet(), hasSize(4));
    }

    @Test
    public void exportRecord() throws IOException {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            csvExportStrategy.exportRecord(outputStream, Arrays.asList("simple", "", "with ,", "with , and \""));
            assertThat(outputStream.toString().trim(), is("simple,,\"with ,\",\"with , and \"\"\""));
        }
    }

    @Test
    public void combineNoValues() {
        assertThat(csvExportStrategy.combineValues(null), is(""));
    }

    @Test
    public void combineSingleValue() {
        assertThat(csvExportStrategy.combineValues(Collections.singletonList("Homer")), is("Homer"));
    }

    @Test
    public void combineMultipleValues() {
        assertThat(csvExportStrategy.combineValues(Arrays.asList("Homer", "Hesiod")), is("Homer, Hesiod"));
    }

    @Test
    public void getExportFormat() {
        assertThat(csvExportStrategy.getExportFormat(), is(ExportFormat.CSV));
    }
}
