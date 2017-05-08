/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InitialLocation {
    final Double latitude;
    final Double longitude;

    public InitialLocation(@JsonProperty("latitude") final Double latitude, @JsonProperty("longitude") final Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
