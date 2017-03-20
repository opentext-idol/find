package com.hp.autonomy.frontend.find.core.configuration.export;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings({"InstanceVariableOfConcreteClass", "WeakerAccess"})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@JsonDeserialize(builder = ExportConfig.ExportConfigBuilder.class)
public class ExportConfig extends SimpleComponent<ExportConfig> {
    private final PowerPointConfig powerpoint;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExportConfigBuilder {}
}
