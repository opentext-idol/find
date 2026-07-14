package com.hp.autonomy.frontend.find.core.beanconfiguration;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.Duration;

/**
 * TODO: this shouldn't be necessary but {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DURATIONS_AS_TIMESTAMPS} doesn't appear to work
 */
@JacksonComponent
public class DurationSerializer extends ValueSerializer<Duration> {
    @Override
    public void serialize(final Duration duration, final JsonGenerator generator, final SerializationContext context) {
        generator.writeNumber(duration.getSeconds());
    }
}
