package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = Trigger.TriggerBuilder.class)
public class Trigger {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> indexes;

    private final String field;
    private final List<String> values;

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class TriggerBuilder {}
}
