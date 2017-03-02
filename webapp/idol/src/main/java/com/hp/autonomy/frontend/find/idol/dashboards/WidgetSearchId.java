package com.hp.autonomy.frontend.find.idol.dashboards;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = WidgetSearchId.WidgetSearchIdBuilder.class)
public class WidgetSearchId extends SimpleComponent<WidgetSearchId> {
    public enum Type {
        QUERY,
        SNAPSHOT
    }

    private final long id;
    private final Type type;

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class WidgetSearchIdBuilder {

    }
}
