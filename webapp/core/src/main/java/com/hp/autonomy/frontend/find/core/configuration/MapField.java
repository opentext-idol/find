/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class MapField {
    final String displayName;
    final String latitudeField;
    final String longitudeField;
    final String geoindexField;
    final String iconName;
    final String iconColor;
    final String markerColor;

    public MapField(
            @JsonProperty("displayName") final String displayName,
            @JsonProperty("latitudeField") final String latitudeField,
            @JsonProperty("longitudeField") final String longitudeField,
            @JsonProperty("geoindexField") final String geoindexField,
            @JsonProperty("iconName") final String iconName,
            @JsonProperty("iconColor") final String iconColor,
            @JsonProperty("markerColor") final String markerColor) {
        this.displayName = displayName;
        this.latitudeField = latitudeField;
        this.longitudeField = longitudeField;
        this.geoindexField = geoindexField;
        this.iconName = iconName;
        this.iconColor = iconColor;
        this.markerColor = markerColor;
    }
}
