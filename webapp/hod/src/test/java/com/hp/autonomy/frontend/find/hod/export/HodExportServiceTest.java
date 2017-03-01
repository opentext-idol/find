/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.export.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HodExportServiceTest {
    @Mock
    private HodDocumentsService documentsService;
    @Mock
    private HodQueryRequestBuilder queryRequestBuilder;
    @Mock
    private HodQueryRequest queryRequest;
    @Mock
    private ExportStrategy exportStrategy;
    @Mock
    private OutputStream outputStream;

    private List<String> fieldNames;
    private HodExportService hodExportService;

    @Before
    public void setUp() {
        when(queryRequest.toBuilder()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.printFields(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.start(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.build()).thenReturn(queryRequest);

        when(exportStrategy.getExportFormat()).thenReturn(ExportFormat.CSV);
        fieldNames = Arrays.asList(HodMetadataNode.REFERENCE.getDisplayName(), HodMetadataNode.DATABASE.getDisplayName(), HodMetadataNode.SUMMARY.getDisplayName(), HodMetadataNode.DATE.getDisplayName(), "authors", "categories", "books", "epic", "lastRead");
        when(exportStrategy.getFieldNames(any(MetadataNode[].class), eq(Collections.emptyList()))).thenReturn(fieldNames);
        final FieldInfo<?> authorInfo = fieldInfo("authors", "author", FieldType.STRING, null);
        final FieldInfo<?> categoryInfo = fieldInfo("categories", "category", FieldType.STRING, null);
        final FieldInfo<?> booksInfo = fieldInfo("books", "category", FieldType.NUMBER, null);
        final FieldInfo<?> epicInfo = fieldInfo("epic", "category", FieldType.BOOLEAN, null);
        final FieldInfo<?> lastReadInfo = fieldInfo("lastRead", "category", FieldType.DATE, null);
        when(exportStrategy.getConfiguredFieldsById()).thenReturn(ImmutableMap.<String, FieldInfo<?>>builder()
                .put("author", authorInfo)
                .put("category", categoryInfo)
                .put("books", booksInfo)
                .put("epic", epicInfo)
                .put("lastRead", lastReadInfo)
                .build());

        hodExportService = new HodExportService(documentsService, new ExportStrategy[]{exportStrategy});
    }

    @Test
    public void export() throws IOException, HodErrorException {
        final HodSearchResult result1 = HodSearchResult.builder()
                .reference("1")
                .index("ClassicalDomain:GreekLiterature")
                .title("The Iliad")
                .summary("Sing goddess of the anger of Achilles")
                .weight(0.51)
                .date(DateTime.now())
                .fieldMap(ImmutableMap.of(
                        "author", fieldInfo("authors", "author", FieldType.STRING, "Homer"),
                        "books", fieldInfo("books", "books", FieldType.NUMBER, 24),
                        "epic", fieldInfo("epic", "epic", FieldType.BOOLEAN, true),
                        "lastRead", fieldInfo("lastRead", "lastRead", FieldType.DATE, DateTime.now())))
                .build();
        final HodSearchResult result2 = HodSearchResult.builder()
                .reference("2")
                .index("ClassicalDomain:GreekLiterature")
                .title("The Theogony")
                .summary("Inspired by the Muses of Mount Helicon let us sing")
                .weight(0.62)
                .date(DateTime.now())
                .fieldMap(ImmutableMap.of("categories", FieldInfo.builder()
                        .id("categories")
                        .name("category")
                        .values(Arrays.asList("Epic Literature", "Philosophy", "Cosmogony"))
                        .build()))
                .build();
        final Documents<HodSearchResult> results = new Documents<>(Arrays.asList(result1, result2), 2, null, null, null, null);
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(results);

        hodExportService.export(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 10L);
        verify(exportStrategy, times(2)).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    private FieldInfo<?> fieldInfo(final String id, final String name, final FieldType type, final Object value) {
        return FieldInfo.builder()
                .id(id)
                .name(name)
                .type(type)
                .advanced(true)
                .value(value)
                .build();
    }

    @Test
    public void exportEmptyResultSetWithoutHeader() throws IOException, HodErrorException {
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(new Documents<>(Collections.emptyList(), 0, null, null, null, null));

        hodExportService.export(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 10L);
        verify(exportStrategy, never()).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test(expected = RuntimeException.class)
    public void unexpectedError() throws IOException, HodErrorException {
        final HodSearchResult result1 = HodSearchResult.builder()
                .reference("1")
                .index("ClassicalDomain:GreekLiterature")
                .title("The Iliad")
                .summary("Sing goddess of the anger of Achilles")
                .weight(0.51)
                .date(DateTime.now())
                .fieldMap(ImmutableMap.of(
                        "author", fieldInfo("authors", "author", FieldType.STRING, "Homer"),
                        "books", fieldInfo("books", "books", FieldType.NUMBER, 24),
                        "epic", fieldInfo("epic", "epic", FieldType.BOOLEAN, true),
                        "lastRead", fieldInfo("lastRead", "lastRead", FieldType.DATE, DateTime.now())))
                .build();
        final HodSearchResult result2 = HodSearchResult.builder()
                .reference("2")
                .index("ClassicalDomain:GreekLiterature")
                .title("The Theogony")
                .summary("Inspired by the Muses of Mount Helicon let us sing")
                .weight(0.62)
                .date(DateTime.now())
                .fieldMap(ImmutableMap.of("categories", FieldInfo.builder()
                        .id("categories")
                        .name("category")
                        .values(Arrays.asList("Epic Literature", "Philosophy", "Cosmogony"))
                        .build()))
                .build();
        final Documents<HodSearchResult> results = new Documents<>(Arrays.asList(result1, result2), 2, null, null, null, null);
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(results);

        doThrow(new IOException("")).when(exportStrategy).exportRecord(eq(outputStream), anyListOf(String.class));

        hodExportService.export(outputStream, queryRequest, ExportFormat.CSV, Collections.singletonList("header"), 10L);
    }
}
