/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.export.service;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.export.service.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.config.FieldValue;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodPlatformDataExportServiceTest {
    @Mock
    private HodDocumentsService documentsService;
    @Mock
    private HodQueryRequestBuilder queryRequestBuilder;
    @Mock
    private HodQueryRequest queryRequest;
    @Mock
    private PlatformDataExportStrategy exportStrategy;
    @Mock
    private OutputStream outputStream;
    @Autowired
    private FieldPathNormaliser fieldPathNormaliser;

    private HodPlatformDataExportService hodExportService;

    @Before
    public void setUp() {
        when(queryRequest.toBuilder()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.printFields(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.start(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.build()).thenReturn(queryRequest);

        when(exportStrategy.getExportFormat()).thenReturn(ExportFormat.CSV);
        final List<FieldInfo<?>> fieldNames = Stream.of(HodMetadataNode.REFERENCE.getDisplayName(), HodMetadataNode.DATABASE.getDisplayName(), HodMetadataNode.SUMMARY.getDisplayName(), HodMetadataNode.DATE.getDisplayName(), "authors", "categories", "books", "epic", "lastRead")
            .map(s -> FieldInfo.builder().id(s).displayName(s).build())
            .collect(Collectors.toList());
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

        hodExportService = new HodPlatformDataExportService(documentsService, new PlatformDataExportStrategy[]{exportStrategy});
    }

    @Test
    public void export() throws IOException, HodErrorException {
        final HodSearchResult result1 = HodSearchResult.builder()
            .reference("1")
            .index("ClassicalDomain:GreekLiterature")
            .title("The Iliad")
            .summary("Sing goddess of the anger of Achilles")
            .weight(0.51)
            .date(ZonedDateTime.now())
            .fieldMap(ImmutableMap.of(
                "author", fieldInfo("authors", "author", FieldType.STRING, "Homer"),
                "books", fieldInfo("books", "books", FieldType.NUMBER, 24),
                "epic", fieldInfo("epic", "epic", FieldType.BOOLEAN, true),
                "lastRead", fieldInfo("lastRead", "lastRead", FieldType.DATE, ZonedDateTime.now())))
            .build();
        final HodSearchResult result2 = HodSearchResult.builder()
            .reference("2")
            .index("ClassicalDomain:GreekLiterature")
            .title("The Theogony")
            .summary("Inspired by the Muses of Mount Helicon let us sing")
            .weight(0.62)
            .date(ZonedDateTime.now())
            .fieldMap(ImmutableMap.of("categories", FieldInfo.builder()
                .id("categories")
                .name(fieldPathNormaliser.normaliseFieldPath("category"))
                .value(new FieldValue<>("Epic Literature", "Epic Literature"))
                .value(new FieldValue<>("Philosophy", "Philosophy"))
                .value(new FieldValue<>("Cosmogony", "Cosmogony"))
                .build()))
            .build();
        final Documents<HodSearchResult> results = new Documents<>(Arrays.asList(result1, result2), 2, null, null, null, null);
        when(documentsService.queryTextIndex(any())).thenReturn(results);

        hodExportService.exportQueryResults(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 10L);
        verify(exportStrategy, times(2)).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    private FieldInfo<?> fieldInfo(final String id, final String name, final FieldType type, final Serializable value) {
        return FieldInfo.builder()
            .id(id)
            .name(fieldPathNormaliser.normaliseFieldPath(name))
            .type(type)
            .advanced(true)
            .value(new FieldValue<>(value, String.valueOf(value)))
            .build();
    }

    @Test
    public void exportEmptyResultSetWithoutHeader() throws IOException, HodErrorException {
        when(documentsService.queryTextIndex(any())).thenReturn(new Documents<>(Collections.emptyList(), 0, null, null, null, null));

        hodExportService.exportQueryResults(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 10L);
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
            .date(ZonedDateTime.now())
            .fieldMap(ImmutableMap.of(
                "author", fieldInfo("authors", "author", FieldType.STRING, "Homer"),
                "books", fieldInfo("books", "books", FieldType.NUMBER, 24),
                "epic", fieldInfo("epic", "epic", FieldType.BOOLEAN, true),
                "lastRead", fieldInfo("lastRead", "lastRead", FieldType.DATE, ZonedDateTime.now())))
            .build();
        final HodSearchResult result2 = HodSearchResult.builder()
            .reference("2")
            .index("ClassicalDomain:GreekLiterature")
            .title("The Theogony")
            .summary("Inspired by the Muses of Mount Helicon let us sing")
            .weight(0.62)
            .date(ZonedDateTime.now())
            .fieldMap(ImmutableMap.of("categories", FieldInfo.builder()
                .id("categories")
                .name(fieldPathNormaliser.normaliseFieldPath("category"))
                .value(new FieldValue<>("Epic Literature", "Epic Literature"))
                .value(new FieldValue<>("Philosophy", "Philosophy"))
                .value(new FieldValue<>("Cosmogony", "Cosmogony"))
                .build()))
            .build();
        final Documents<HodSearchResult> results = new Documents<>(Arrays.asList(result1, result2), 2, null, null, null, null);
        when(documentsService.queryTextIndex(any())).thenReturn(results);

        doThrow(new IOException("")).when(exportStrategy).exportRecord(eq(outputStream), anyListOf(String.class));

        hodExportService.exportQueryResults(outputStream, queryRequest, ExportFormat.CSV, Collections.singletonList("header"), 10L);
    }
}
