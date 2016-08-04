/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.export.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
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
    private DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService;
    @Mock
    private ExportStrategy exportStrategy;
    @Mock
    private OutputStream outputStream;
    @Mock
    private SearchRequest<ResourceIdentifier> searchRequest;

    private List<String> fieldNames;
    private HodExportService hodExportService;

    @Before
    public void setUp() {
        when(exportStrategy.getExportFormat()).thenReturn(ExportFormat.CSV);
        fieldNames = Arrays.asList(HodMetadataNode.REFERENCE.getName(), HodMetadataNode.DATABASE.getName(), HodMetadataNode.SUMMARY.getName(), HodMetadataNode.DATE.getName(), "authors", "categories", "books", "epic", "lastRead");
        when(exportStrategy.getFieldNames(any(MetadataNode[].class))).thenReturn(fieldNames);
        final FieldInfo<?> authorInfo = fieldInfo("authors", "author", FieldType.STRING, null);
        final FieldInfo<?> categoryInfo = fieldInfo("categories", "category", FieldType.STRING, null);
        final FieldInfo<?> booksInfo = fieldInfo("books", "category", FieldType.NUMBER, null);
        final FieldInfo<?> epicInfo = fieldInfo("epic", "category", FieldType.BOOLEAN, null);
        final FieldInfo<?> lastReadInfo = fieldInfo("lastRead", "category", FieldType.DATE, null);
        when(exportStrategy.getConfiguredFields()).thenReturn(ImmutableMap.<String, FieldInfo<?>>builder()
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
        when(exportStrategy.writeHeader()).thenReturn(true);

        final HodSearchResult result1 = new HodSearchResult.Builder()
                .setReference("1")
                .setIndex("ClassicalDomain:GreekLiterature")
                .setTitle("The Iliad")
                .setSummary("Sing goddess of the anger of Achilles")
                .setWeight(0.51)
                .setDate(DateTime.now())
                .setFieldMap(ImmutableMap.of(
                        "author", fieldInfo("authors", "author", FieldType.STRING, "Homer"),
                        "books", fieldInfo("books", "books", FieldType.NUMBER, 24),
                        "epic", fieldInfo("epic", "epic", FieldType.BOOLEAN, true),
                        "lastRead", fieldInfo("lastRead", "lastRead", FieldType.DATE, DateTime.now())))
                .build();
        final HodSearchResult result2 = new HodSearchResult.Builder()
                .setReference("2")
                .setIndex("ClassicalDomain:GreekLiterature")
                .setTitle("The Theogony")
                .setSummary("Inspired by the Muses of Mount Helicon let us sing")
                .setWeight(0.62)
                .setDate(DateTime.now())
                .setFieldMap(ImmutableMap.of("categories", new FieldInfo<>("categories", Collections.singleton("category"), FieldType.STRING, false, Arrays.asList("Epic Literature", "Philosophy", "Cosmogony"))))
                .build();
        final Documents<HodSearchResult> results = new Documents<>(Arrays.asList(result1, result2), 2, null, null, null, null);
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(results);

        hodExportService.export(outputStream, searchRequest, ExportFormat.CSV);
        verify(exportStrategy, times(3)).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    private FieldInfo<?> fieldInfo(final String id, final String name, final FieldType type, final Object value) {
        return new FieldInfo<>(id, Collections.singleton(name), type, true, value);
    }

    @Test
    public void exportEmptyResultSetWithHeader() throws IOException, HodErrorException {
        when(exportStrategy.writeHeader()).thenReturn(true);
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(new Documents<>(Collections.emptyList(), 0, null, null, null, null));

        hodExportService.export(outputStream, searchRequest, ExportFormat.CSV);
        verify(exportStrategy).exportRecord(outputStream, fieldNames);
    }

    @Test
    public void exportEmptyResultSetWithoutHeader() throws IOException, HodErrorException {
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(new Documents<>(Collections.emptyList(), 0, null, null, null, null));

        hodExportService.export(outputStream, searchRequest, ExportFormat.CSV);
        verify(exportStrategy, never()).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test(expected = RuntimeException.class)
    public void unexpectedError() throws IOException, HodErrorException {
        when(exportStrategy.writeHeader()).thenReturn(true);
        when(documentsService.queryTextIndex(Matchers.any())).thenReturn(new Documents<>(Collections.emptyList(), 0, null, null, null, null));
        doThrow(new IOException("")).when(exportStrategy).exportRecord(eq(outputStream), anyListOf(String.class));
        hodExportService.export(outputStream, searchRequest, ExportFormat.CSV);
    }
}
