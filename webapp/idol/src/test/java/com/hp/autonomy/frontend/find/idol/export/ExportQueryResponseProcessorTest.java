/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExportQueryResponseProcessorTest {
    @Mock
    private ExportStrategy exportStrategy;

    private ByteArrayOutputStream outputStream;
    private List<String> fieldNames;
    private ExportQueryResponseProcessor processor;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        processor = new ExportQueryResponseProcessor(exportStrategy, outputStream, Collections.emptyList());
        fieldNames = Arrays.asList("Reference", "Database", "Summary", "Date", "categories");
        when(exportStrategy.getFieldNames(any(MetadataNode[].class), eq(Collections.emptyList()))).thenReturn(fieldNames);
        when(exportStrategy.getConfiguredFieldsById()).thenReturn(ImmutableMap.of("CATEGORY", FieldInfo.<String>builder()
                .id("categories")
                .name("CATEGORY")
                .build()));

    }

    @Test
    public void export() throws IOException {
        processor.process(new MockAciResponseInputStream(IdolExportServiceTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/export/query-response.xml")));
        verify(exportStrategy, times(6)).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test
    public void exportEmptyResultSetWithoutHeader() throws IOException {
        processor.process(new MockAciResponseInputStream(IOUtils.toInputStream("<?xml version='1.0' encoding='UTF-8' ?>\n<autnresponse><response/></autnresponse>")));
        verify(exportStrategy, never()).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test(expected = AciErrorException.class)
    public void errorResponse() {
        processor.process(new MockAciResponseInputStream(IdolExportServiceTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/export/error-response.xml")));
    }

    @Test(expected = ProcessorException.class)
    public void unexpectedError() {
        processor.process(new MockAciResponseInputStream(IOUtils.toInputStream("")));
    }

    private static class MockAciResponseInputStream extends AciResponseInputStream {
        private MockAciResponseInputStream(final InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public String getHeader(final String name) {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public String getContentType() {
            return "text/xml";
        }
    }
}
