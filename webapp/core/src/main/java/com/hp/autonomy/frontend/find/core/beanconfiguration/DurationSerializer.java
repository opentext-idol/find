package com.hp.autonomy.frontend.find.core.beanconfiguration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.Duration;

/**
 * TODO: this shouldn't be necessary but {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DURATIONS_AS_TIMESTAMPS} doesn't appear to work
 */
@JsonComponent
public class DurationSerializer extends JsonSerializer<Duration> {
    @Override
    public void serialize(final Duration duration, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        generator.writeNumber(duration.getSeconds());
    }
}
