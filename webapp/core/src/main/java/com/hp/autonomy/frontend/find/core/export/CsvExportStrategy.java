/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.google.common.base.Strings;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CsvExportStrategy implements ExportStrategy {
    // Excel can't cope with CSV files without a BOM (FIND-498)
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final ConfigService<? extends HavenSearchCapable> configService;
    private final FieldPathNormaliser fieldPathNormaliser;

    @Autowired
    public CsvExportStrategy(final ConfigService<? extends HavenSearchCapable> configService,
                             final FieldPathNormaliser fieldPathNormaliser) {
        this.configService = configService;
        this.fieldPathNormaliser = fieldPathNormaliser;
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

        final Stream<String> nonMetadataStream = getFieldsInfo().getFieldConfig()
                .keySet()
                .stream()
                // Filters parametric (non-metadata) fields
                .filter(id -> selectedFieldIds.isEmpty() || selectedFieldIds.contains(id));

        return Stream.concat(metadataStream, nonMetadataStream).collect(Collectors.toList());
    }

    @Override
    public Map<String, FieldInfo<?>> getConfiguredFieldsById() {
        return getFieldsInfo().getFieldConfig();
    }

    @Override
    public Optional<FieldInfo<?>> getFieldInfoForNode(final String nodeName) {
        return Optional.ofNullable(getFieldsInfo().getFieldConfigByName().get(fieldPathNormaliser.normaliseFieldPath(nodeName)));
    }

    @Override
    public void exportRecord(final OutputStream outputStream, final Iterable<String> values) throws IOException {
        try (final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream), CSVFormat.EXCEL)) {
            csvPrinter.printRecord(values);
        }
    }

    @Override
    // CAUTION: Method has more than one exit point.
    public String combineValues(final List<String> maybeValues) {
        return Optional.ofNullable(maybeValues)
                .map(values -> StringUtils.join(values.stream().filter(val -> !Strings.isNullOrEmpty(val)).collect(Collectors.toList()), ", "))
                .orElse("");
    }

    @Override
    public ExportFormat getExportFormat() {
        return ExportFormat.CSV;
    }

    private FieldsInfo getFieldsInfo() {
        return configService.getConfig().getFieldsInfo();
    }
}
