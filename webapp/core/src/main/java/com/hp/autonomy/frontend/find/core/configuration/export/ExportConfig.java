package com.hp.autonomy.frontend.find.core.configuration.export;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings({"InstanceVariableOfConcreteClass", "WeakerAccess"})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@JsonDeserialize(builder = ExportConfig.ExportConfigBuilder.class)
public class ExportConfig extends SimpleComponent<ExportConfig> implements OptionalConfigurationComponent<ExportConfig> {
    private final PowerPointConfig powerpoint;

    @Override
    public Boolean getEnabled() {
        return true;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExportConfigBuilder {}
}
