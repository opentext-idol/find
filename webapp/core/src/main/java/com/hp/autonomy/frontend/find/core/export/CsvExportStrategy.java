/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CsvExportStrategy implements ExportStrategy {
    private final ConfigService<? extends HavenSearchCapable> configService;
    private final byte[] outputPrefix = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};/*UTF-8 BOM*/

    @Autowired
    public CsvExportStrategy(final ConfigService<? extends HavenSearchCapable> configService) {
        this.configService = configService;
    }

    @Override
    public boolean prependOutput() {
        return true;
    }

    @Override
    public boolean writeHeader() {
        return true;
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
        for (final FieldInfo<?> field : fieldConfig) {
            for (final String name : field.getNames()) {
                configuredFieldINames.put(name, field);
            }
        }

        return configuredFieldINames;
    }

    @Override
    public void exportRecord(final OutputStream outputStream, final Iterable<String> values) throws IOException {
        // TODO: Here we are creating a disposable CSVPrinter for every. Single. Record. Would be faster to have a
        // persistent printer as a strategy's member object and make better use of its API (flush, etc.).
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

    @Override
    public byte[] getOutputPrefix() {
        return outputPrefix;
    }

    private Collection<FieldInfo<?>> getFieldConfig() {
        return configService.getConfig().getFieldsInfo().getFieldConfig().values();
    }
}
