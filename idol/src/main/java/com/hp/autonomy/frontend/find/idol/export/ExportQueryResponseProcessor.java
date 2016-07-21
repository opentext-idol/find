/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.convert.converter.Converter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
class ExportQueryResponseProcessor extends AbstractStAXProcessor<Void> {
    private static final String HIT_NODE_NAME = "autn:hit";

    static final Converter<String, String> STRING_CONVERTER = new Converter<String, String>() {
        @Override
        public String convert(final String source) {
            return source;
        }
    };

    static final Converter<String, String> DATE_CONVERTER = new Converter<String, String>() {
        @Override
        public String convert(final String source) {
            final long epoch = Long.parseLong(source) * 1000;
            final DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
            return formatter.print(epoch);
        }
    };

    private static final int METADATA_LENGTH = IdolMetadataNode.values().length;
    private static final Map<String, IdolMetadataNode> METADATA_NODES = new HashMap<>(METADATA_LENGTH);
    private static final Map<FieldType, Converter<String, String>> converterMap = new EnumMap<>(FieldType.class);

    static {
        for (final IdolMetadataNode metadataNode : IdolMetadataNode.values()) {
            METADATA_NODES.put(metadataNode.getNodeName(), metadataNode);
        }

        converterMap.put(FieldType.STRING, STRING_CONVERTER);
        converterMap.put(FieldType.NUMBER, STRING_CONVERTER);
        converterMap.put(FieldType.BOOLEAN, STRING_CONVERTER);
        converterMap.put(FieldType.DATE, DATE_CONVERTER);
    }

    private final ExportStrategy exportStrategy;
    private final OutputStream outputStream;
    private final Map<String, FieldInfo<?>> configuredFields;

    ExportQueryResponseProcessor(final ExportStrategy exportStrategy, final OutputStream outputStream) {
        this.exportStrategy = exportStrategy;
        this.outputStream = outputStream;
        configuredFields = exportStrategy.getConfiguredFields();
    }

    @Override
    public Void process(final XMLStreamReader aciResponse) throws AciErrorException, ProcessorException {
        final Collection<String> fieldNames = exportStrategy.getFieldNames(IdolMetadataNode.values());

        try {
            if (exportStrategy.writeHeader()) {
                exportStrategy.exportRecord(outputStream, fieldNames);
            }

            while (aciResponse.hasNext()) {
                final int eventType = aciResponse.next();

                if (XMLEvent.START_ELEMENT == eventType) {
                    if (HIT_NODE_NAME.equals(aciResponse.getLocalName())) {
                        parseHit(fieldNames, aciResponse);
                    }
                }
            }
        } catch (final XMLStreamException | IOException e) {
            throw new ProcessorException("Error parsing data", e);
        }
        return null;
    }

    private void parseHit(final Collection<String> fieldNames, final XMLStreamReader aciResponse) throws XMLStreamException, IOException {
        final Map<String, List<String>> valueMap = new HashMap<>(fieldNames.size());

        int eventType;
        while (aciResponse.hasNext() && !((eventType = aciResponse.next()) == XMLEvent.END_ELEMENT && HIT_NODE_NAME.equals(aciResponse.getLocalName()))) {
            if (XMLEvent.START_ELEMENT == eventType) {
                final String nodeName = aciResponse.getLocalName();
                if (METADATA_NODES.containsKey(nodeName)) {
                    final IdolMetadataNode metadataNode = METADATA_NODES.get(nodeName);
                    final String name = metadataNode.getName();
                    final String value = metadataNode.getConverter().convert(aciResponse.getElementText().trim());
                    addValueToMap(valueMap, name, value);
                } else if (configuredFields.containsKey(nodeName)) {
                    final FieldInfo<?> fieldInfo = configuredFields.get(nodeName);
                    final String name = fieldInfo.getId();
                    final String value = converterMap.get(fieldInfo.getType()).convert(aciResponse.getElementText().trim());
                    addValueToMap(valueMap, name, value);
                }
            }
        }

        final Collection<String> values = new ArrayList<>(fieldNames.size());
        for (final String fieldName : fieldNames) {
            final List<String> fieldValues = valueMap.get(fieldName);
            values.add(exportStrategy.combineValues(fieldValues));
        }

        exportStrategy.exportRecord(outputStream, values);
    }

    private void addValueToMap(final Map<String, List<String>> valueMap, final String name, final String value) {
        if (!valueMap.containsKey(name)) {
            valueMap.put(name, new ArrayList<String>());
        }

        valueMap.get(name).add(value);
    }
}