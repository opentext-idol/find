/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unused", "SpringJavaAutowiredMembersInspection"})
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CoreTestContext.class, CsvExportStrategy.class},
        properties = CORE_CLASSES_PROPERTY,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CsvExportStrategyTest {
    @Autowired
    private FieldPathNormaliser fieldPathNormaliser;
    @MockBean
    private ConfigService<HavenSearchCapable> configService;
    @Mock
    private HavenSearchCapable config;
    @Autowired
    private CsvExportStrategy csvExportStrategy;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
        when(config.getFieldsInfo()).thenReturn(FieldsInfo.builder()
                .populateResponseMap("authors", FieldInfo.<String>builder()
                        .id("authors")
                        .name(fieldPathNormaliser.normaliseFieldPath("AUTHOR"))
                        .name(fieldPathNormaliser.normaliseFieldPath("author"))
                        .build())
                .populateResponseMap("categories", FieldInfo.<String>builder()
                        .id("categories")
                        .name(fieldPathNormaliser.normaliseFieldPath("CATEGORY"))
                        .name(fieldPathNormaliser.normaliseFieldPath("category"))
                        .build())
                .populateResponseMap("databases", FieldInfo.<String>builder()
                        .id("databases")
                        .name(fieldPathNormaliser.normaliseFieldPath("DATABASE"))
                        .name(fieldPathNormaliser.normaliseFieldPath("database"))
                        .build())
                .build());
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
