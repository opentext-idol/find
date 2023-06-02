/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.autonomy.frontend.configuration.ConfigException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class MapConfiguration {
    private final String tileUrlTemplate;
    private final Boolean enabled;
    private final String attribution;
    private final List<MapField> locationFields;
    private final Integer resultsStep;
    private final InitialLocation initialLocation;

    public MapConfiguration(
            @JsonProperty("tileUrlTemplate") final String tileUrlTemplate,
            @JsonProperty("enabled") final Boolean enabled,
            @JsonProperty("attribution") final String attribution,
            @JsonProperty("locationFields") final List<MapField> locationFields,
            @JsonProperty("resultsStep") final Integer resultsStep,
            @JsonProperty("initialLocation") final InitialLocation initialLocation
    ) {
        this.tileUrlTemplate = tileUrlTemplate;
        this.enabled = enabled;
        this.attribution = attribution;
        this.locationFields = locationFields;
        this.resultsStep = resultsStep;
        this.initialLocation = initialLocation;
    }

    public MapConfiguration merge(final MapConfiguration other) {
        if(other == null) {
            return this;
        } else {
            return new MapConfiguration(
                    tileUrlTemplate == null ? other.tileUrlTemplate : tileUrlTemplate,
                    enabled == null ? other.enabled : enabled,
                    attribution == null ? other.attribution : attribution,
                    locationFields == null ? other.locationFields : locationFields,
                    resultsStep == null ? other.resultsStep : resultsStep,
                    initialLocation == null ? other.initialLocation : initialLocation
            );
        }
    }

    public void basicValidate(final String configSection) throws ConfigException {
        if(enabled) {
            if(StringUtils.isBlank(tileUrlTemplate)) {
                throw new ConfigException(configSection, "tileUrlTemplate must be provided");
            }
        }
    }
}
