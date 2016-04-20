/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsDeserializer;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class IdolQueryRestrictionsDeserializer extends QueryRestrictionsDeserializer<String> {
    public IdolQueryRestrictionsDeserializer() {
        super(new StringNodeParser());
    }

    @Override
    public QueryRestrictions<String> deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final ObjectMapper objectMapper = createObjectMapper();

        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        return new IdolQueryRestrictions.Builder()
                .setQueryText(parseAsText(objectMapper, node, "queryText"))
                .setFieldText(parseAsText(objectMapper, node, "fieldText"))
                .setDatabases(parseDatabaseArray(node, "databases"))
                .setMinDate(parseDate(objectMapper, node, "minDate"))
                .setMaxDate(parseDate(objectMapper, node, "maxDate"))
                .setLanguageType(parseAsText(objectMapper, node, "languageType"))
                .setStateMatchId(parseStringArray(node, "stateMatchId"))
                .setStateDontMatchId(parseStringArray(node, "stateDontMatchId"))
                .setAnyLanguage(parseAsBoolean(objectMapper, node, "anyLanguage"))
                .build();
    }

    protected class StringDatabaseNodeParser implements NodeParser<String> {
        @Override
        public String parse(final JsonNode databaseNode) {
            return databaseNode.asText();
        }
    }
}

