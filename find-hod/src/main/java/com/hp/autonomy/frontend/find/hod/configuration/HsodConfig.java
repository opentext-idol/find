/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URL;

@Data
@JsonDeserialize(builder = HsodConfig.Builder.class)
public class HsodConfig {
    private final URL landingPageUrl;
    private final URL externalUrl;
    private final URL findAppUrl;

    private HsodConfig(final Builder builder) {
        landingPageUrl = builder.landingPageUrl;
        externalUrl = builder.externalUrl;
        findAppUrl = builder.findAppUrl;
    }

    public HsodConfig merge(final HsodConfig other) {
        if (other == null) {
            return this;
        }

        return new Builder()
                .setLandingPageUrl(landingPageUrl == null ? other.landingPageUrl : landingPageUrl)
                .setExternalUrl(externalUrl == null ? other.externalUrl : externalUrl)
                .setFindAppUrl(findAppUrl == null ? other.findAppUrl : findAppUrl)
                .build();
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    @Accessors(chain = true)
    public static class Builder {
        private URL landingPageUrl;
        private URL externalUrl;
        private URL findAppUrl;

        public HsodConfig build() {
            return new HsodConfig(this);
        }
    }
}
