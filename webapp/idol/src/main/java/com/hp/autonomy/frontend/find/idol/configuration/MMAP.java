package com.hp.autonomy.frontend.find.idol.configuration;

/*
 * Copyright 2015 Open Text.
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@JsonDeserialize(builder = MMAP.Builder.class)
public class MMAP implements OptionalConfigurationComponent<MMAP> {
    private final Boolean enabled;
    private final String baseUrl;

    private MMAP(final Builder builder) {
        enabled = builder.enabled;
        baseUrl = builder.baseUrl;
    }

    @Override
    public MMAP merge(final MMAP mmap) {
        return mmap == null ? this : new Builder()
                .setBaseUrl(baseUrl == null ? mmap.baseUrl : baseUrl)
                .setEnabled(enabled == null ? mmap.enabled : enabled)
                .build();
    }

    @Override
    public void basicValidate(final String s) throws ConfigException {
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
