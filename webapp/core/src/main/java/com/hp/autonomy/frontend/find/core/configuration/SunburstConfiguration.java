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
import lombok.Data;

@Data
public class SunburstConfiguration {
    /**
     * Set this to false to avoid the error case where field configuration between databases is
     * inconsistent.  This doesn't help with inconsistent field configuration between databases
     * merged using DAH.
     */
    private final Boolean allowMultipleDatabases;

    public SunburstConfiguration(
        @JsonProperty("allowMultipleDatabases") final Boolean allowMultipleDatabases
    ) {
        this.allowMultipleDatabases = allowMultipleDatabases;
    }

    public SunburstConfiguration merge(final SunburstConfiguration other) {
        if(other == null) {
            return this;
        } else {
            return new SunburstConfiguration(
                allowMultipleDatabases == null ?
                    other.allowMultipleDatabases : allowMultipleDatabases
            );
        }
    }

}
