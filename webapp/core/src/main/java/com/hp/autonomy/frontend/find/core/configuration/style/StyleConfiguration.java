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

package com.hp.autonomy.frontend.find.core.configuration.style;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = StyleConfiguration.StyleConfigurationBuilder.class)
public class StyleConfiguration extends AbstractConfig<StyleConfiguration> {
    private final Map<String, String> simpleVariables;
    private final String termHighlightColor;
    private final String termHighlightBackground;

    @Override
    public StyleConfiguration merge(final StyleConfiguration other) {
        return ConfigurationUtils.defaultMerge(this, other);
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {}

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class StyleConfigurationBuilder {}
}
