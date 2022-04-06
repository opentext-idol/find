/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.export.service;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
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
    @Autowired
    private FieldDisplayNameGenerator fieldDisplayNameGenerator;
    @MockBean
    private ConfigService<HavenSearchCapable> configService;
    @Mock
    private HavenSearchCapable config;
    @Autowired
    private CsvExportStrategy csvExportStrategy;
    private FieldInfo<String> authorInfo;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
        authorInfo = FieldInfo.<String>builder()
                .id("authors")
                .name(fieldPathNormaliser.normaliseFieldPath("AUTHOR"))
                .name(fieldPathNormaliser.normaliseFieldPath("author"))
                .displayName("Authors")
                .value(new FieldValue<>("Aiskhulos", "Aeschylus"))
                .build();
        when(config.getFieldsInfo()).thenReturn(FieldsInfo.builder()
                .populateResponseMap("authors", authorInfo)
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
        csvExportStrategy.writeHeader(byteArrayOutputStream, Stream.of("header1", "header2", "header3")
                .map(s -> FieldInfo.builder().displayName(s).build())
                .collect(Collectors.toList()));
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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void getFieldInfoForMetadataNode() {
        final MetadataNode metadataInfo = new ReferenceMetadataNode();
        final Optional<? extends FieldInfo<?>> maybeReferenceInfo = csvExportStrategy.getFieldInfoForMetadataNode("reference", ImmutableMap.of("reference", metadataInfo), Collections.emptyList());
        assertTrue(maybeReferenceInfo.isPresent());
        assertEquals("Reference", maybeReferenceInfo.get().getDisplayName());
    }

    @Test
    public void getFieldInfoForUnselectedMetadataNode() {
        final MetadataNode metadataInfo = new ReferenceMetadataNode();
        final Optional<? extends FieldInfo<?>> maybeReferenceInfo = csvExportStrategy.getFieldInfoForMetadataNode("reference", ImmutableMap.of("reference", metadataInfo), Collections.singletonList("bad"));
        assertFalse(maybeReferenceInfo.isPresent());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void getFieldInfoForKnownNode() {
        final Optional<? extends FieldInfo<?>> maybeAuthorInfo = csvExportStrategy.getFieldInfoForNode("author", Collections.singletonList("authors"));
        assertTrue(maybeAuthorInfo.isPresent());
        assertEquals("Authors", maybeAuthorInfo.get().getDisplayName());
    }

    @Test
    public void getFieldInfoForUnselectedKnownNode() {
        final Optional<? extends FieldInfo<?>> maybeAuthorInfo = csvExportStrategy.getFieldInfoForNode("author", Collections.singletonList("bad"));
        assertFalse(maybeAuthorInfo.isPresent());
    }

    @Test
    public void getFieldInfoForUnknownNode() {
        assertFalse(csvExportStrategy.getFieldInfoForNode("bad", Collections.singletonList("bad")).isPresent());
    }

    @Test
    public void getDisplayValue() {
        assertEquals("Aeschylus", csvExportStrategy.getDisplayValue(authorInfo, "Aiskhulos"));
    }

    @Test
    public void getDisplayValueUnmappedValue() {
        assertEquals("Homer", csvExportStrategy.getDisplayValue(authorInfo, "Homer"));
    }

    @Test
    public void getDisplayValueNullValue() {
        assertNull(csvExportStrategy.getDisplayValue(authorInfo, null));
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

    private static class ReferenceMetadataNode implements MetadataNode {
        @Override
        public String getDisplayName() {
            return "Reference";
        }

        @Override
        public FieldType getFieldType() {
            return FieldType.STRING;
        }

        @Override
        public String getName() {
            return "reference";
        }
    }
}
