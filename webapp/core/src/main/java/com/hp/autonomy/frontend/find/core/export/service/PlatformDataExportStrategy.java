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

import com.hp.autonomy.searchcomponents.core.config.FieldInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Export implementation for a particular {@link ExportFormat}
 */
public interface PlatformDataExportStrategy {
    /**
     * Write the headers to the file and do anything else necessary to prepare the file before the results.
     */
    void writeHeader(OutputStream outputStream, Collection<FieldInfo<?>> fieldNames) throws IOException;

    /**
     * Retrieves the names of all the fields to export
     *
     * @param metadataNodes    hard metadata (HoD/Idol specific)
     * @param selectedFieldIds only export fields with ids in this collection. If empty, export all fields
     * @return the names of all the metadata/fields to export
     */
    List<FieldInfo<?>> getFieldNames(MetadataNode[] metadataNodes, final Collection<String> selectedFieldIds);

    /**
     * Returns the fields configured for export in the config file. Inverse lookup of getConfiguredFieldsByName().
     *
     * @return a map of field ID as it appears in the frontend to field information
     */
    Map<String, FieldInfo<?>> getConfiguredFieldsById();

    /**
     * Returns field information for metadata.
     *
     * @param nodeName response field node being parsed
     * @param metadataNodes known metadata information
     * @return field information retrieved from config
     */
    Optional<FieldInfo<Serializable>> getFieldInfoForMetadataNode(final String nodeName, final Map<String, ? extends MetadataNode> metadataNodes, final Collection<String> selectedFieldIds);

    /**
     * Returns field information retrieved from configuration.
     *
     * @param nodePath response field node being parsed
     * @return field information retrieved from config
     */
    Optional<FieldInfo<?>> getFieldInfoForNode(final String nodePath, final Collection<String> selectedFieldIds);

    /**
     * Returns the display value to use in export
     *
     * @param fieldInfo field information
     * @param value     field value
     * @param <T>       field type
     * @return field display value
     */
    <T extends Serializable> String getDisplayValue(final FieldInfo<?> fieldInfo, final T value);

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
