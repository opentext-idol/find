package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class InitialLocation {
    final Double latitude;
    final Double longitude;

    public InitialLocation(@JsonProperty("latitude") final Double latitude, @JsonProperty("longitude") final Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
