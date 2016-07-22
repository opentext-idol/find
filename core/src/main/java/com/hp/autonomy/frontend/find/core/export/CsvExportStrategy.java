/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CsvExportStrategy implements ExportStrategy {
    private final ConfigService<? extends HavenSearchCapable> configService;

    @Autowired
    public CsvExportStrategy(final ConfigService<? extends HavenSearchCapable> configService) {
        this.configService = configService;
    }

    @Override
    public boolean writeHeader() {
        return true;
    }

    @Override
    public List<String> getFieldNames(final MetadataNode[] metadataNodes) {
        final List<String> fieldNames = new ArrayList<>(metadataNodes.length);
        for (final MetadataNode metadataNode : metadataNodes) {
            fieldNames.add(metadataNode.getName());
        }

        for (final FieldInfo<?> field : getFieldConfig()) {
            fieldNames.add(field.getId());
        }

        return fieldNames;
    }

    @Override
    public Map<String, FieldInfo<?>> getConfiguredFields() {
        final Map<String, FieldInfo<?>> configuredFields = new LinkedHashMap<>();
        final Collection<FieldInfo<?>> fieldConfig = getFieldConfig();
        for (final FieldInfo<?> field : fieldConfig) {
            for (final String name : field.getNames()) {
                configuredFields.put(name, field);
            }
        }

        return configuredFields;
    }

    @Override
    public void exportRecord(final OutputStream outputStream, final Iterable<String> fieldNames) throws IOException {
        try (final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.EXCEL)) {
            csvPrinter.printRecord(fieldNames);
        }
    }

    @Override
    public String combineValues(final List<String> values) {
        return values == null ? "" : StringUtils.join(values, ", ");
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    private Collection<FieldInfo<?>> getFieldConfig() {
        final FieldsInfo fieldsInfo = configService.getConfig().getFieldsInfo();
        return fieldsInfo.getFieldConfig().values();
    }
}
