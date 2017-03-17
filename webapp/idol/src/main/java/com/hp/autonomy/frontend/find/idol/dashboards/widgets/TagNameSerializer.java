package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;

import java.io.IOException;

/**
 * Custom deserialization of {@link FieldInfo}
 */
public class TagNameSerializer extends JsonSerializer<TagName> {
    @Override
    public void serialize(final TagName value, final JsonGenerator jsonGenerator, final SerializerProvider serializers) throws IOException {
        jsonGenerator.writeString(value.getId().getNormalisedPath());
    }
}
