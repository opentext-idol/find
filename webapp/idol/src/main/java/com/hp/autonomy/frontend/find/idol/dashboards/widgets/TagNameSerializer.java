package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Custom deserialization of {@link FieldInfo}
 */
public class TagNameSerializer extends ValueSerializer<TagName> {
    @Override
    public void serialize(final TagName value, final JsonGenerator jsonGenerator, final SerializationContext context) {
        jsonGenerator.writeString(value.getId().getNormalisedPath());
    }
}
