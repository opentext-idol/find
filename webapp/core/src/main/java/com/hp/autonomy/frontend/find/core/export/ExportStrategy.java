/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.searchcomponents.core.config.FieldInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Export implementation for a particular {@link ExportFormat}
 */
public interface ExportStrategy {
    /**
     * Write the headers to the file and do anything else necessary to prepare the file before the results.
     */
    void writeHeader(OutputStream outputStream, Collection<String> fieldNames) throws IOException;

    /**
     * Retrieves the names of all the fields to export
     *
     * @param metadataNodes    hard metadata (HoD/Idol specific)
     * @param selectedFieldIds only export fields with ids in this collection. If empty, export all fields
     * @return the names of all the metadata/fields to export
     */
    List<String> getFieldNames(MetadataNode[] metadataNodes, final Collection<String> selectedFieldIds);

    /**
     * Returns the fields configured for export in the config file. Inverse lookup of getConfiguredFieldsByName().
     *
     * @return a map of field ID as it appears in the frontend to field information
     */
    Map<String, FieldInfo<?>> getConfiguredFieldsById();

    /**
     * Returns the fields configured for export in the config file. Inverse lookup of getConfiguredFieldsById().
     *
     * @return a map of field name as it appears in the server response to field information
     */
    Map<String, FieldInfo<?>> getConfiguredFieldsByName();

    /**
     * Exports all the data corresponding to an individual document to the given {@link OutputStream}
     *
     * @param outputStream the stream to which the formatted data will be written
     * @param fieldNames   the names of the metadata/fields being exported
     * @throws IOException any I/O error
     */
    void exportRecord(OutputStream outputStream, Iterable<String> fieldNames) throws IOException;

    /**
     * Converts any field values into a single combined value
     *
     * @param values the values of a particular document field (only more than one for an array field)
     * @return the combined value
     */
    String combineValues(List<String> values);

    /**
     * The format with which this strategy is associated
     *
     * @return the {@link ExportFormat} associated with this format
     */
    ExportFormat getExportFormat();

}
