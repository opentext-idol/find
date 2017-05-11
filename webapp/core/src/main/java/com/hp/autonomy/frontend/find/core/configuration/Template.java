package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = Template.TemplateBuilder.class)
public class Template extends SimpleComponent<Template> implements OptionalConfigurationComponent<Template>{
    private final String file;
    private final List<Trigger> triggers;

    @Override
    public Boolean getEnabled() {
        return !triggers.isEmpty() && StringUtils.isNotBlank(file);
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class TemplateBuilder {}
}
