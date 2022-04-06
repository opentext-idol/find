/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ExportQueryResponseProcessorTest {
    @Autowired
    private FieldPathNormaliser fieldPathNormaliser;
    @Mock
    private PlatformDataExportStrategy exportStrategy;

    private ByteArrayOutputStream outputStream;
    private ExportQueryResponseProcessor processor;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        final List<FieldInfo<?>> fieldNames = Stream.of("Reference", "Database", "Summary", "Date", "categories")
            .map(s -> FieldInfo.builder().id(s).displayName(s).build())
            .collect(Collectors.toList());
        processor = new ExportQueryResponseProcessor(exportStrategy, outputStream, fieldNames, Collections.emptyList());
        when(exportStrategy.getFieldInfoForMetadataNode(anyString(), any(), any())).thenReturn(Optional.empty());
        when(exportStrategy.getFieldInfoForNode(anyString(), any())).thenReturn(Optional.empty());
        final Optional<FieldInfo<?>> optional = Optional.of(FieldInfo.<String>builder()
                                                                .id("categories")
                                                                .name(fieldPathNormaliser.normaliseFieldPath("CATEGORY"))
                                                                .build());
        when(exportStrategy.getFieldInfoForNode(eq("CATEGORY"), any())).thenReturn(optional);
    }

    @Test
    public void export() throws IOException {
        processor.process(new MockAciResponseInputStream(IdolPlatformDataExportServiceTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/export/query-response.xml")));
        verify(exportStrategy, times(6)).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test
    public void exportEmptyResultSetWithoutHeader() throws IOException {
        processor.process(new MockAciResponseInputStream(IOUtils.toInputStream("<?xml version='1.0' encoding='UTF-8' ?>\n<autnresponse><response/></autnresponse>")));
        verify(exportStrategy, never()).exportRecord(eq(outputStream), anyListOf(String.class));
    }

    @Test
    public void errorResponse() {
        try {
            processor.process(new MockAciResponseInputStream(IdolPlatformDataExportServiceTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/export/error-response.xml")));
            fail("Exception should have been thrown");
        } catch(final AciErrorException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("No query text supplied"));
        }
    }

    @Test
    public void unexpectedError() {
        try {
            processor.process(new MockAciResponseInputStream(IOUtils.toInputStream("")));
            fail("Exception should have been thrown");
        } catch(final ProcessorException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Error parsing data"));
        }
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
