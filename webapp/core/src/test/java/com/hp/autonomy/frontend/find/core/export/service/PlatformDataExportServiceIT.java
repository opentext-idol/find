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

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRequestBuilder;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.test.TestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
public abstract class PlatformDataExportServiceIT<R extends QueryRequest<Q>, Q extends QueryRestrictions<?>, E extends Exception> {
    @Autowired
    private PlatformDataExportService<R, E> exportService;

    @Autowired
    private TestUtils<Q> testUtils;

    @Autowired
    private ObjectFactory<QueryRequestBuilder<R, Q, ?>> queryRequestBuilderFactory;

    @Test
    public void exportToCsv() throws E, IOException {
        final R queryRequest = queryRequestBuilderFactory.getObject()
                .queryRestrictions(testUtils.buildQueryRestrictions())
                .queryType(QueryRequest.QueryType.MODIFIED)
                .build();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportService.exportQueryResults(outputStream, queryRequest, ExportFormat.CSV, Collections.emptyList(), 1001L);
        final String output = outputStream.toString();
        assertNotNull(output);

        try (final CSVParser csvParser = CSVParser.parse(output, CSVFormat.EXCEL)) {
            final List<CSVRecord> records = csvParser.getRecords();
            assertThat(records, not(empty()));
            final CSVRecord headerRecord = records.get(0);
            assertThat(headerRecord.get(0), endsWith("Reference")); // byte-order mark may get in the way
            assertEquals("Database", headerRecord.get(1));
            final CSVRecord firstDataRecord = records.get(1);
            final String firstDataRecordReference = firstDataRecord.get(0);
            assertNotNull(firstDataRecordReference);
            assertFalse(firstDataRecordReference.trim().isEmpty());
            final String firstDataRecordDatabase = firstDataRecord.get(1);
            assertFalse(firstDataRecordDatabase.trim().isEmpty());
        }
    }
}
