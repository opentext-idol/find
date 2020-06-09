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

import com.google.common.base.Strings;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.config.HavenSearchCapable;
import com.hp.autonomy.searchcomponents.core.fields.FieldDisplayNameGenerator;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CsvExportStrategy implements PlatformDataExportStrategy {
    // Excel can't cope with CSV files without a BOM (FIND-498)
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final ConfigService<? extends HavenSearchCapable> configService;
    private final FieldPathNormaliser fieldPathNormaliser;
    private final FieldDisplayNameGenerator fieldDisplayNameGenerator;

    @Autowired
    public CsvExportStrategy(final ConfigService<? extends HavenSearchCapable> configService,
                             final FieldPathNormaliser fieldPathNormaliser,
                             final FieldDisplayNameGenerator fieldDisplayNameGenerator) {
        this.configService = configService;
        this.fieldPathNormaliser = fieldPathNormaliser;
        this.fieldDisplayNameGenerator = fieldDisplayNameGenerator;
    }

    @Override
    public void writeHeader(final OutputStream outputStream, final Collection<FieldInfo<?>> fieldNames) throws IOException {
        outputStream.write(UTF8_BOM);
        exportRecord(outputStream, fieldNames.stream()
                .map(FieldInfo::getDisplayName)
                .collect(Collectors.toList()));
    }

    @Override
    public List<FieldInfo<?>> getFieldNames(final MetadataNode[] metadataNodes, final Collection<String> selectedFieldIds) {
        final Stream<FieldInfo<?>> metadataStream = Arrays.stream(metadataNodes)
                // Filters metadata fields
                .filter(metadataNode -> selected(selectedFieldIds, metadataNode.getName()))
                .map(this::metadataNodeToFieldInfo);

        final Stream<FieldInfo<?>> nonMetadataStream = getFieldsInfo().getFieldConfig()
                .values()
                .stream()
                // Filters parametric (non-metadata) fields
                .filter(fieldInfo -> selected(selectedFieldIds, fieldInfo.getId()))
                .map(this::populateMissingDisplayNames);

        return Stream.concat(metadataStream, nonMetadataStream).collect(Collectors.toList());
    }

    @Override
    public Map<String, FieldInfo<?>> getConfiguredFieldsById() {
        return getFieldsInfo().getFieldConfig();
    }

    @Override
    public Optional<FieldInfo<Serializable>> getFieldInfoForMetadataNode(final String nodeName, final Map<String, ? extends MetadataNode> metadataNodes, final Collection<String> selectedFieldIds) {
        return Optional.ofNullable(metadataNodes.get(nodeName))
                .map(this::metadataNodeToFieldInfo)
                .filter(fieldInfo -> selected(selectedFieldIds, fieldInfo.getId()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<FieldInfo<?>> getFieldInfoForNode(final String nodeName, final Collection<String> selectedFieldIds) {
        final Optional<FieldInfo<?>> maybeFieldInfo = Optional.ofNullable(getFieldsInfo().getFieldConfigByName().get(fieldPathNormaliser.normaliseFieldPath(nodeName)));
        return maybeFieldInfo
                .filter(fieldInfo -> selected(selectedFieldIds, fieldInfo.getId()));
    }

    @Override
    public <T extends Serializable> String getDisplayValue(final FieldInfo<?> fieldInfo, final T value) {
        return fieldDisplayNameGenerator.parseDisplayValue(() -> Optional.of(fieldInfo), value);
    }

    @Override
    public void exportRecord(final OutputStream outputStream, final Iterable<String> values) throws IOException {
        try (final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), CSVFormat.EXCEL)) {
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

    private <T extends Serializable> FieldInfo<T> metadataNodeToFieldInfo(final MetadataNode metadataNode) {
        return FieldInfo.<T>builder()
                .id(metadataNode.getName())
                .displayName(metadataNode.getDisplayName())
                .type(metadataNode.getFieldType())
                .build();
    }

    private <T extends Serializable> FieldInfo<T> populateMissingDisplayNames(final FieldInfo<T> fieldInfo) {
        return Optional.ofNullable(fieldInfo.getDisplayName())
                .map(x -> fieldInfo)
                .orElseGet(() -> fieldInfo.toBuilder()
                        .displayName(fieldDisplayNameGenerator.prettifyFieldName(fieldInfo.getId()))
                        .build());
    }

    private boolean selected(final Collection<String> selectedFieldIds, final String id) {
        return selectedFieldIds.isEmpty() || selectedFieldIds.contains(id);
    }
}
