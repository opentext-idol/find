/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.export.service;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.ProcessorException;
import com.autonomy.aci.client.services.impl.AbstractStAXProcessor;
import com.autonomy.aci.client.services.impl.ErrorProcessor;
import com.hp.autonomy.frontend.find.core.export.service.PlatformDataExportStrategy;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
class ExportQueryResponseProcessor extends AbstractStAXProcessor<Void> {
    private static final String HIT_NODE_NAME = "autn:hit";
    private static final String CONTENT_NODE_NAME = "autn:content";

    private static final Map<String, IdolMetadataNode> METADATA_NODES = new HashMap<>();

    static {
        for (final IdolMetadataNode metadataNode : IdolMetadataNode.values()) {
            METADATA_NODES.put(metadataNode.getNodeName(), metadataNode);
        }
    }

    private final PlatformDataExportStrategy exportStrategy;
    private final OutputStream outputStream;
    private final Collection<FieldInfo<?>> fieldInfoList;
    private final Collection<String> selectedFieldIds;

    ExportQueryResponseProcessor(final PlatformDataExportStrategy exportStrategy, final OutputStream outputStream, final Collection<FieldInfo<?>> fieldInfoList, final Collection<String> selectedFieldIds) {
        this.outputStream = outputStream;
        this.fieldInfoList = new ArrayList<>(fieldInfoList);
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

            while (aciResponse.hasNext()) {
                aciResponse.next();

                if (aciResponse.isStartElement()) {
                    if (HIT_NODE_NAME.equals(aciResponse.getLocalName())) {
                        parseHit(aciResponse);
                    }
                }
            }
        } catch (final XMLStreamException | IOException e) {
            throw new ProcessorException("Error parsing data", e);
        }
        return null;
    }

    private void parseHit(final XMLStreamReader aciResponse) throws XMLStreamException, IOException {
        final Map<String, List<String>> valueMap = new HashMap<>();

        boolean hitEnd = false;
        while (aciResponse.hasNext() && !hitEnd) {
            aciResponse.next();
            if (aciResponse.isStartElement()) {
                final String nodeName = aciResponse.getLocalName();
                if (CONTENT_NODE_NAME.equals(nodeName)) {
                    parseContent(aciResponse, valueMap);
                } else {
                    conditionallyAddMetadataValueToExportMap(aciResponse, valueMap, nodeName);
                }
            } else if (aciResponse.isEndElement()) {
                hitEnd = HIT_NODE_NAME.equals(aciResponse.getLocalName());
            }
        }

        final Collection<String> values = fieldInfoList.stream()
                .map(fieldInfo -> exportStrategy.combineValues(valueMap.get(fieldInfo.getId())))
                .collect(Collectors.toList());
        exportStrategy.exportRecord(outputStream, values);
    }

    private void parseContent(final XMLStreamReader aciResponse, final Map<String, List<String>> valueMap) throws XMLStreamException {
        boolean contentEnd = false;
        final Stack<String> nodes = new Stack<>();
        while (aciResponse.hasNext() && !contentEnd) {
            aciResponse.next();
            if (aciResponse.isStartElement()) {
                final String nodeName = aciResponse.getLocalName();
                nodes.push(nodeName);
                conditionallyAddFieldValueToExportMap(aciResponse, valueMap, nodeName, nodes);
            } else if (aciResponse.isEndElement()) {
                contentEnd = CONTENT_NODE_NAME.equals(aciResponse.getLocalName());
                if (!contentEnd) {
                    nodes.pop();
                }
            }
        }
    }

    private void conditionallyAddMetadataValueToExportMap(final XMLStreamReader aciResponse, final Map<String, List<String>> valueMap, final String nodeName) throws XMLStreamException {
        final Optional<? extends FieldInfo<?>> maybeFieldInfo = exportStrategy.getFieldInfoForMetadataNode(nodeName, METADATA_NODES, selectedFieldIds);

        if (maybeFieldInfo.isPresent()) {
            final FieldInfo<?> fieldInfo = maybeFieldInfo.get();
            addValueToMap(aciResponse, valueMap, fieldInfo);
        }
    }

    private void conditionallyAddFieldValueToExportMap(final XMLStreamReader aciResponse, final Map<String, List<String>> valueMap, final String nodeName, final Stack<String> nodes) throws XMLStreamException {
        final String nodePath = CollectionUtils.isEmpty(nodes) ? nodeName : String.join("/", nodes);
        final Optional<? extends FieldInfo<?>> maybeFieldInfo = exportStrategy.getFieldInfoForNode(nodePath, selectedFieldIds);

        if (maybeFieldInfo.isPresent()) {
            final FieldInfo<?> fieldInfo = maybeFieldInfo.get();
            addValueToMap(aciResponse, valueMap, fieldInfo);
            nodes.pop();
        }
    }

    private void addValueToMap(final XMLStreamReader aciResponse, final Map<String, List<String>> valueMap, final FieldInfo<?> fieldInfo) throws XMLStreamException {
        final String id = fieldInfo.getId();
        final FieldType fieldType = fieldInfo.getType();
        final Object rawValue = parseValue(aciResponse, fieldType);
        final String value = StringUtils.defaultString(exportStrategy.getDisplayValue(fieldInfo, (Serializable) rawValue));

        if (!valueMap.containsKey(id)) {
            valueMap.put(id, new ArrayList<>());
        }

        valueMap.get(id).add(value);
    }

    private Object parseValue(final XMLStreamReader aciResponse, final FieldType fieldType) throws XMLStreamException {
        return fieldType.parseValue(fieldType.getType(), aciResponse.getElementText().trim());
    }
}
