package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class ParametricDisplayValue {
    final String name;
    final String displayName;

    public ParametricDisplayValue(@JsonProperty("name") final String name, @JsonProperty("displayName") final String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
}
