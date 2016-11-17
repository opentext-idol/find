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
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.function.Function;

@JsonComponent
public class IdolQueryRestrictionsDeserializer extends QueryRestrictionsDeserializer<String> {
    @Override
    public QueryRestrictions<String> deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final ObjectMapper objectMapper = createObjectMapper();

        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        return IdolQueryRestrictions.builder()
                .queryText(parseAsText(objectMapper, node, "queryText"))
                .fieldText(parseAsText(objectMapper, node, "fieldText"))
                .databases(parseDatabaseArray(node, "databases"))
                .minDate(parseDate(objectMapper, node, "minDate"))
                .maxDate(parseDate(objectMapper, node, "maxDate"))
                .languageType(parseAsText(objectMapper, node, "languageType"))
                .stateMatchIds(parseStringArray(node, "stateMatchId"))
                .stateDontMatchIds(parseStringArray(node, "stateDontMatchId"))
                .anyLanguage(parseAsBoolean(objectMapper, node, "anyLanguage"))
                .build();
    }

    @Override
    protected Function<JsonNode, String> constructDatabaseNodeParser() {
        return JsonNode::asText;
    }
}

