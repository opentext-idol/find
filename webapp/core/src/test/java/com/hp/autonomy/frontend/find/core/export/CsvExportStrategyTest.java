/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
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
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsvExportStrategyTest {
    @Mock
    private ConfigService<HavenSearchCapable> configService;
    @Mock
    private HavenSearchCapable config;

    private CsvExportStrategy csvExportStrategy;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
        when(config.getFieldsInfo()).thenReturn(FieldsInfo.builder()
                .populateResponseMap("authors", FieldInfo.<String>builder()
                        .id("authors")
                        .names(Arrays.asList("AUTHOR", "author"))
                        .build())
                .populateResponseMap("categories", FieldInfo.<String>builder()
                        .id("categories")
                        .names(Arrays.asList("CATEGORY", "category"))
                        .build())
                .populateResponseMap("databases", FieldInfo.<String>builder()
                        .id("databases")
                        .names(Arrays.asList("DATABASE", "database"))
                        .build())
                .build());

        csvExportStrategy = new CsvExportStrategy(configService);
    }

    @Test
    public void writeHeader() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        csvExportStrategy.writeHeader(byteArrayOutputStream, Arrays.asList("header1", "header2", "header3"));
        assertThat(byteArrayOutputStream.toString(), endsWith("header1,header2,header3\r\n"));
    }

    @Test
    public void getFieldNames() {
        assertThat(csvExportStrategy.getFieldNames(new MetadataNode[]{mock(MetadataNode.class)}, Collections.emptyList()), hasSize(4));
    }

    @Test
    public void getConfiguredFields() {
        assertThat(csvExportStrategy.getConfiguredFieldsById().keySet(), hasSize(3));
    }

    @Test
    public void getFilteredFields() {
        assertThat(csvExportStrategy.getFieldNames(new MetadataNode[]{mock(MetadataNode.class)}, Collections.singletonList("authors")), hasSize(1));
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
