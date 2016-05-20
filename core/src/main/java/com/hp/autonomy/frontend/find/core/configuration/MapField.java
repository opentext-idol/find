package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class MapField {
    final String displayName;
    final String latitudeField;
    final String longitudeField;
    final String iconName;
    final String iconColor;
    final String markerColor;

    public MapField(
            @JsonProperty("displayName") final String displayName,
            @JsonProperty("latitudeField") final String latitudeField,
            @JsonProperty("longitudeField") final String longitudeField,
            @JsonProperty("iconName") final String iconName,
            @JsonProperty("iconColor") final String iconColor,
            @JsonProperty("markerColor") final String markerColor) {
        this.displayName = displayName;
        this.latitudeField = latitudeField;
        this.longitudeField = longitudeField;
        this.iconName = iconName;
        this.iconColor = iconColor;
        this.markerColor = markerColor;
    }
}
