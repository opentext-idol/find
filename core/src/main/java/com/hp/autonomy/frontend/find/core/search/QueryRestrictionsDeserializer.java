/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class QueryRestrictionsDeserializer<S extends Serializable> extends JsonDeserializer<QueryRestrictions<S>> {
    private final NodeParser<S> databaseNodeParser;

    protected QueryRestrictionsDeserializer(NodeParser<S> databaseNodeParser) {
        this.databaseNodeParser = databaseNodeParser;
    }

    protected ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        return objectMapper;
    }

    protected <T> T parseAs(final ObjectMapper objectMapper, @SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName, final Class<T> type) throws JsonProcessingException {
        final JsonNode childNode = node.get(fieldName);
        return childNode == null ? null : objectMapper.treeToValue(childNode, type);
    }

    protected String parseAsText(final ObjectMapper objectMapper, final JsonNode node, final String fieldName) throws JsonProcessingException {
        return parseAs(objectMapper, node, fieldName, String.class);
    }

    protected DateTime parseDate(final ObjectMapper objectMapper, final JsonNode node, final String fieldName) throws IOException {
        return parseAs(objectMapper, node, fieldName, DateTime.class);
    }

    protected boolean parseAsBoolean(final ObjectMapper objectMapper, final JsonNode node, final String fieldName) throws JsonProcessingException {
        return parseAs(objectMapper, node, fieldName, Boolean.class);
    }

    protected List<S> parseDatabaseArray(@SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName) {
        return parseArray(node, fieldName, databaseNodeParser);
    }
    
    protected List<String> parseStringArray(@SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName) {
        return parseArray(node, fieldName, new StringNodeParser());
    }

    protected <T> List<T> parseArray(@SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName, final NodeParser<T> parser) {
        final List<T> fields = new ArrayList<>();
        final JsonNode arrayNode = node.get(fieldName);
        if (arrayNode != null) {
            final Iterator<JsonNode> iterator = arrayNode.elements();
            while (iterator.hasNext()) {
                fields.add(parser.parse(iterator.next()));
            }
        }

        return fields;
    }

    protected static class StringNodeParser implements NodeParser<String> {
        public StringNodeParser() {}

        @Override
        public String parse(final JsonNode jsonNode) {
            return jsonNode.asText();
        }
    }

    protected interface NodeParser<T> {
        T parse(final JsonNode jsonNode);
    }
}