package com.hp.autonomy.frontend.find.idol.configuration;

/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.BooleanUtils;

@Data
@JsonDeserialize(builder = MMAP.Builder.class)
public class MMAP implements ConfigurationComponent {
    private final Boolean enabled;
    private final String baseUrl;

    private MMAP(final Builder builder) {
        enabled = builder.enabled;
        baseUrl = builder.baseUrl;
    }

    public MMAP merge(final MMAP mmap) {
        if (mmap == null) {
            return this;
        } else {
            return new Builder()
                    .setBaseUrl(baseUrl == null ? mmap.baseUrl : baseUrl)
                    .setEnabled(enabled == null ? mmap.enabled : enabled)
                    .build();
        }
    }

    @Override
    public boolean isEnabled() {
        return BooleanUtils.isTrue(enabled);
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String baseUrl;
        private Boolean enabled;

        public MMAP build() {
            return new MMAP(this);
        }
    }
}
