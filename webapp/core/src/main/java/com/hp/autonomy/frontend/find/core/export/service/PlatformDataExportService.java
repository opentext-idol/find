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

package com.hp.autonomy.frontend.find.core.export.service;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Service for exporting search result sets to some other format.
 *
 * @param <R> request type to use
 */
public interface PlatformDataExportService<R extends QueryRequest<?>, E extends Exception> {
    int PAGINATION_SIZE = 1000;

    /**
     * Performs query and exports the returned result set to the specified format.
     *
     * @param outputStream     the output to which the resulting format is written
     * @param queryRequest     the search request parameters
     * @param exportFormat     the format to exportQueryResults to
     * @param selectedFieldIds only exportQueryResults fields with ids enumerated this collection. If empty, exportQueryResults all fields
     * @param totalResults     the total number of results from the query to paginate over
     * @throws E if an error is thrown by the underlying server, the corresponding thrown exception
     */
    void exportQueryResults(OutputStream outputStream,
                            R queryRequest,
                            ExportFormat exportFormat,
                            Collection<String> selectedFieldIds,
                            long totalResults) throws E, IOException;

    /**
     * Export formats supported by this service
     *
     * @return the formats supported by this service
     */
    Collection<ExportFormat> handlesFormats();
}
