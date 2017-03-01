/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.google.common.base.Strings;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CsvExportStrategy implements ExportStrategy {
    // Excel can't cope with CSV files without a BOM (FIND-498)
    private static final byte[] UTF8_BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};

    private final ConfigService<? extends HavenSearchCapable> configService;

    @Autowired
    public CsvExportStrategy(final ConfigService<? extends HavenSearchCapable> configService) {
        this.configService = configService;
    }

    @Override
    public void writeHeader(final OutputStream outputStream, final Collection<String> fieldNames) throws IOException {
        outputStream.write(UTF8_BOM);
        exportRecord(outputStream, fieldNames);
    }

    @Override
    public List<String> getFieldNames(final MetadataNode[] metadataNodes, final Collection<String> selectedFieldIds) {
        final Stream<String> metadataStream = Arrays.stream(metadataNodes)
                // Filters metadata fields
                .filter(metadataNode -> selectedFieldIds.isEmpty() || selectedFieldIds.contains(metadataNode.getName()))
                .map(MetadataNode::getDisplayName);

        final Stream<String> nonMetadataStream = getFieldConfig().stream()
                .map(FieldInfo::getId)
                // Filters parametric (non-metadata) fields
                .filter(id -> selectedFieldIds.isEmpty() || selectedFieldIds.contains(id));

        return Stream.concat(metadataStream, nonMetadataStream).collect(Collectors.toList());
    }

    @Override
    public Map<String, FieldInfo<?>> getConfiguredFieldsById() {
        final Map<String, FieldInfo<?>> configuredFieldIds = new LinkedHashMap<>();

        getFieldConfig().forEach(field -> configuredFieldIds.put(field.getId(), field));

        return configuredFieldIds;
    }

    @Override
    public Map<String, FieldInfo<?>> getConfiguredFieldsByName() {
        final Map<String, FieldInfo<?>> configuredFieldINames = new LinkedHashMap<>();
        final Collection<FieldInfo<?>> fieldConfig = getFieldConfig();
        for(final FieldInfo<?> field : fieldConfig) {
            for(final String name : field.getNames()) {
                configuredFieldINames.put(name, field);
            }
        }

        return configuredFieldINames;
    }

    @Override
    public void exportRecord(final OutputStream outputStream, final Iterable<String> values) throws IOException {
        try(final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.EXCEL)) {
            csvPrinter.printRecord(values);
        }
    }

    @Override
    // CAUTION: Method has more than one exit point.
    public String combineValues(final List<String> values) {
        if(values == null) {
            return "";
        } else {
            return StringUtils.join(values.stream().filter(val -> !Strings.isNullOrEmpty(val)).collect(Collectors.toList()), ", ");
        }
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    private Collection<FieldInfo<?>> getFieldConfig() {
        return configService.getConfig().getFieldsInfo().getFieldConfig().values();
    }
}
