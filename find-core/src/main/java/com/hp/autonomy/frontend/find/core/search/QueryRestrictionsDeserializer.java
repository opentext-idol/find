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
    protected ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        return objectMapper;
    }

    protected String parseAsText(final ObjectMapper objectMapper, @SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName) throws JsonProcessingException {
        final JsonNode jsonNode = node.get(fieldName);
        return jsonNode != null ? objectMapper.treeToValue(jsonNode, String.class) : null;
    }

    protected List<S> parseDatabaseArray(@SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName) {
        final List<S> fields = new ArrayList<>();
        final JsonNode databasesNode = node.get(fieldName);
        if (databasesNode != null) {
            final Iterator<JsonNode> iterator = databasesNode.elements();
            while (iterator.hasNext()) {
                fields.add(parseDatabaseNode(iterator.next()));
            }
        }

        return fields;
    }

    protected List<String> parseStringArray(@SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName) {
        final List<String> fields = new ArrayList<>();
        final JsonNode databasesNode = node.get(fieldName);
        if (databasesNode != null) {
            final Iterator<JsonNode> iterator = databasesNode.elements();
            while (iterator.hasNext()) {
                fields.add(iterator.next().asText());
            }
        }

        return fields;
    }

    protected abstract S parseDatabaseNode(final JsonNode databaseNode);

    protected DateTime parseDate(final ObjectMapper objectMapper, @SuppressWarnings("TypeMayBeWeakened") final JsonNode node, final String fieldName) throws IOException {
        final JsonNode childNode = node.get(fieldName);
        return childNode != null ? objectMapper.treeToValue(childNode, DateTime.class) : null;
    }
}