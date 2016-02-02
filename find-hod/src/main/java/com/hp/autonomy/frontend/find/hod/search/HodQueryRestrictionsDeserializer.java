/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsDeserializer;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HodQueryRestrictionsDeserializer extends QueryRestrictionsDeserializer<ResourceIdentifier> {
    public HodQueryRestrictionsDeserializer() {
        super(new ResourceIdentifierNodeParser());
    }

    @Override
    public QueryRestrictions<ResourceIdentifier> deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final ObjectMapper objectMapper = createObjectMapper();

        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        return new HodQueryRestrictions.Builder()
                .setQueryText(parseAsText(objectMapper, node, "queryText"))
                .setFieldText(parseAsText(objectMapper, node, "fieldText"))
                .setDatabases(parseDatabaseArray(node, "databases"))
                .setMinDate(parseDate(objectMapper, node, "minDate"))
                .setMaxDate(parseDate(objectMapper, node, "maxDate"))
                .setLanguageType(parseAsText(objectMapper, node, "languageType"))
                .build();
    }

    protected static class ResourceIdentifierNodeParser implements NodeParser<ResourceIdentifier> {
        @Override
        public ResourceIdentifier parse(final JsonNode databaseNode) {
            return new ResourceIdentifier(databaseNode.get("domain").asText(), databaseNode.get("name").asText());
        }
    }
}
