/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.searchcomponents.core.search.SearchRequest;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * Service for exporting search result sets to some other format.
 *
 * @param <S> database/index object type
 */
public interface ExportService<S extends Serializable, E extends Exception> {
    /**
     * Performs query and exports the returned result set to the specified format.
     *
     * @param outputStream the output to which the resulting format is written
     * @param searchRequest the search request parameters
     * @param exportFormat the format to export to
     * @exception E if an error is thrown by the underlying server, the corresponding thrown exception
     */
    void export(OutputStream outputStream, SearchRequest<S> searchRequest, ExportFormat exportFormat) throws E;
}
