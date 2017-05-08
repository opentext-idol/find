/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.applications;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("WeakerAccess")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = IdolCustomApplication.IdolCustomApplicationBuilder.class)
public class IdolCustomApplication extends SimpleComponent<IdolCustomApplication> implements OptionalConfigurationComponent<IdolCustomApplication> {
    private final String applicationName;
    private final String url;
    private final String icon;
    private final Boolean enabled;
    private final boolean openInNewTab;

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class IdolCustomApplicationBuilder {
        private String icon = "";
        private Boolean enabled = true;
    }
}
