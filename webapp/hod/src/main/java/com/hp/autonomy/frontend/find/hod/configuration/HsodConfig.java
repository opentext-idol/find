/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.net.URL;

@Getter
@Builder
@SuppressWarnings("DefaultAnnotationParam")
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = HsodConfig.HsodConfigBuilder.class)
public class HsodConfig extends SimpleComponent<HsodConfig> implements ConfigurationComponent<HsodConfig> {
    private final URL landingPageUrl;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if(landingPageUrl == null) {
            throw new ConfigException(section, "Landing page URL must be provided");
        }
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class HsodConfigBuilder {}
}
