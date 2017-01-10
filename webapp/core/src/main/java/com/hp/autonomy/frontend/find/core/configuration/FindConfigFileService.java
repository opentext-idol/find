package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.AbstractAuthenticatingConfigFileService;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.jasypt.util.text.TextEncryptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * Common logic applied to HoD and Idol config implementations
 */
public abstract class FindConfigFileService<C extends FindConfig<C, B>, B extends FindConfigBuilder<C, B>> extends AbstractAuthenticatingConfigFileService<C> {
    private static final String CONFIG_FILE_LOCATION = "hp.find.home";
    private static final String CONFIG_FILE_NAME = "config.json";

    protected FindConfigFileService(final FilterProvider filterProvider,
                                    final TextEncryptor textEncryptor,
                                    final JsonDeserializer<TagName> tagNameDeserializer) {
        final ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .mixIns(customMixins())
                .serializers(new TagNameSerializer())
                .deserializersByType(ImmutableMap.of(TagName.class, tagNameDeserializer))
                .createXmlMapper(false)
                .build();

        setConfigFileLocation(CONFIG_FILE_LOCATION);
        setConfigFileName(CONFIG_FILE_NAME);
        setDefaultConfigFile(getDefaultConfigFile());
        setMapper(objectMapper);
        setTextEncryptor(textEncryptor);
        setFilterProvider(filterProvider);
    }

    protected abstract String getDefaultConfigFile();

    protected abstract Map<Class<?>, Class<?>> customMixins();

    @Override
    public void postInitialise(final C config) {
        postUpdate(config);
    }

    @Override
    public C preUpdate(final C config) {
        return config;
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void postUpdate(final C config) {
    }

    static class TagNameSerializer extends JsonSerializer<TagName> {
        @Override
        public void serialize(final TagName value, final JsonGenerator jsonGenerator, final SerializerProvider provider) throws IOException {
            jsonGenerator.writeString(value.getId());
        }

        @Override
        public Class<TagName> handledType() {
            return TagName.class;
        }
    }
}
