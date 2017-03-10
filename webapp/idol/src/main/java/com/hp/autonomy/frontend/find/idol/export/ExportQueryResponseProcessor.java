/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import com.autonomy.aci.client.services.impl.ErrorProcessor;
import com.hp.autonomy.frontend.find.core.export.ExportStrategy;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("serial")
class ExportQueryResponseProcessor extends AbstractStAXProcessor<Void> {
    private static final String HIT_NODE_NAME = "autn:hit";

    private static final Map<String, IdolMetadataNode> METADATA_NODES = new HashMap<>();

    static {
        for (final IdolMetadataNode metadataNode : IdolMetadataNode.values()) {
            METADATA_NODES.put(metadataNode.getNodeName(), metadataNode);
        }
    }

    private final ExportStrategy exportStrategy;
    private final OutputStream outputStream;
    private final Collection<String> selectedFieldIds;

    ExportQueryResponseProcessor(final ExportStrategy exportStrategy, final OutputStream outputStream, final Collection<String> selectedFieldIds) {
        this.outputStream = outputStream;
        this.selectedFieldIds = new ArrayList<>(selectedFieldIds);

        this.exportStrategy = exportStrategy;
    }

    @Override
    public Void process(final XMLStreamReader aciResponse) throws AciErrorException, ProcessorException {
        try {
            if (isErrorResponse(aciResponse)) {
                setErrorProcessor(new ErrorProcessor());
                processErrorResponse(aciResponse);
            }

            final Collection<String> fieldNames = exportStrategy.getFieldNames(IdolMetadataNode.values(), selectedFieldIds);

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
        final Map<String, List<String>> valueMap = new HashMap<>();
        int eventType;

        while (aciResponse.hasNext() && !((eventType = aciResponse.next()) == XMLEvent.END_ELEMENT && HIT_NODE_NAME.equals(aciResponse.getLocalName()))) {
            if (XMLEvent.START_ELEMENT == eventType) {
                final String nodeName = aciResponse.getLocalName();
                final IdolMetadataNode metadataNode = METADATA_NODES.get(nodeName);
                final Optional<FieldInfo<?>> maybeFieldInfo = exportStrategy.getFieldInfoForNode(nodeName);

                if (metadataNode != null || maybeFieldInfo.isPresent()) {
                    final FieldType fieldType;
                    final String id;

                    if (metadataNode != null) {
                        id = metadataNode.getDisplayName();
                        fieldType = metadataNode.getFieldType();
                    } else {
                        @SuppressWarnings("OptionalGetWithoutIsPresent")
                        final FieldInfo<?> fieldInfo = maybeFieldInfo.get();
                        id = fieldInfo.getId();
                        fieldType = fieldInfo.getType();
                    }

                    final Object value = fieldType.parseValue(fieldType.getType(), aciResponse.getElementText().trim());
                    addValueToMap(valueMap, id, value == null ? "" : value.toString());
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
            valueMap.put(name, new ArrayList<>());
        }

        valueMap.get(name).add(value);
    }
}