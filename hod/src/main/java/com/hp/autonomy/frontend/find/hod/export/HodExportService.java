/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.ExportFormat;
import com.hp.autonomy.frontend.find.core.export.ExportService;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.joda.time.ReadableInstant;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class HodExportService implements ExportService<ResourceIdentifier, HodErrorException> {
    private static final Map<FieldType, Converter<Object, String>> CONVERTER_MAP = new EnumMap<>(FieldType.class);

    static {
        CONVERTER_MAP.put(FieldType.STRING, new Converter<Object, String>() {
            @Override
            public String convert(final Object source) {
                return (String) source;
            }
        });
        CONVERTER_MAP.put(FieldType.BOOLEAN, new Converter<Object, String>() {
            @Override
            public String convert(final Object source) {
                return source.toString();
            }
        });
        CONVERTER_MAP.put(FieldType.NUMBER, new Converter<Object, String>() {
            @Override
            public String convert(final Object source) {
                return source.toString();
            }
        });
        CONVERTER_MAP.put(FieldType.DATE, new Converter<Object, String>() {
            @Override
            public String convert(final Object source) {
                return ISODateTimeFormat.dateTime().print((ReadableInstant) source);
            }
        });
    }

    private final DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService;
    private final Map<ExportFormat, ExportStrategy> exportStrategies;

    @Autowired
    public HodExportService(final DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService,
                            final ExportStrategy[] exportStrategies) {
        this.documentsService = documentsService;

        this.exportStrategies = new EnumMap<>(ExportFormat.class);
        for (final ExportStrategy exportStrategy : exportStrategies) {
            this.exportStrategies.put(exportStrategy.getExportFormat(), exportStrategy);
        }
    }

    @Override
    public void export(final OutputStream outputStream, final SearchRequest<ResourceIdentifier> searchRequest, final ExportFormat exportFormat) throws HodErrorException {
        final ExportStrategy exportStrategy = exportStrategies.get(exportFormat);
        final List<String> fieldNames = exportStrategy.getFieldNames(HodMetadataNode.values());

        final Documents<HodSearchResult> documents = documentsService.queryTextIndex(searchRequest);
        try {
            if (exportStrategy.writeHeader()) {
                exportStrategy.exportRecord(outputStream, fieldNames);
            }

            for (final HodSearchResult searchResult : documents.getDocuments()) {
                final Collection<String> values = new ArrayList<>(fieldNames.size());
                values.add(searchResult.getReference());
                values.add(searchResult.getIndex());
                values.add(searchResult.getTitle());
                values.add(searchResult.getSummary());
                values.add(String.valueOf(searchResult.getWeight()));
                values.add(ISODateTimeFormat.dateTime().print(searchResult.getDate()));

                for (final FieldInfo<?> configuredField : exportStrategy.getConfiguredFields().values()) {
                    final FieldInfo<?> fieldInfo = searchResult.getFieldMap().get(configuredField.getId());
                    values.add(exportStrategy.combineValues(getValuesAsStrings(fieldInfo)));
                }

                exportStrategy.exportRecord(outputStream, values);
            }
        } catch (final IOException e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException("Error parsing data", e);
        }
    }

    private List<String> getValuesAsStrings(final FieldInfo<?> fieldInfo) {
        final List<String> stringList;
        if (fieldInfo != null) {
            stringList = new ArrayList<>(fieldInfo.getValues().size());
            for (final Object value : fieldInfo.getValues()) {
                stringList.add(CONVERTER_MAP.get(fieldInfo.getType()).convert(value));
            }
        } else {
            stringList = Collections.emptyList();
        }

        return stringList;
    }
}
