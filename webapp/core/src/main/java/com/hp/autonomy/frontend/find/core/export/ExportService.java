/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.searchcomponents.core.search.QueryRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Service for exporting search result sets to some other format.
 *
 * @param <R> request type to use
 */
@FunctionalInterface
public interface ExportService<R extends QueryRequest<?>, E extends Exception> {

    int PAGINATION_SIZE = 1000;

    /**
     * Performs query and exports the returned result set to the specified format.
     *
     * @param outputStream     the output to which the resulting format is written
     * @param queryRequest     the search request parameters
     * @param exportFormat     the format to export to
     * @param selectedFieldIds only export fields with ids enumerated this collection. If empty, export all fields
     * @param totalResults     the total number of results from the query to paginate over
     * @throws E if an error is thrown by the underlying server, the corresponding thrown exception
     */
    void export(OutputStream outputStream,
                R queryRequest,
                ExportFormat exportFormat,
                Collection<String> selectedFieldIds,
                long totalResults) throws E, IOException;
}
