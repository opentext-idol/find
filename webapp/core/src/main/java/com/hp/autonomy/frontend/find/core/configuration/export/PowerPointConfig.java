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

package com.hp.autonomy.frontend.find.core.configuration.export;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = PowerPointConfig.PowerPointConfigBuilder.class)
public class PowerPointConfig extends SimpleComponent<PowerPointConfig> implements OptionalConfigurationComponent<PowerPointConfig> {
    private static final String SECTION = "PowerPoint Config";

    private final String templateFile;
    private final Double marginTop, marginLeft, marginRight, marginBottom;

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (Stream.of(marginTop, marginLeft, marginRight, marginBottom).anyMatch(value -> value != null && (value >= 1 || value < 0))) {
            throw new ConfigException(SECTION, "All specified margin settings must be populated with values between 0 (inclusive) and 1 (exclusive)");
        }

        if (differenceIsInvalid(marginLeft, marginRight) || differenceIsInvalid(marginTop, marginBottom)) {
            throw new ConfigException(SECTION, "Top/bottom and left/right margin setting pairs should sum to less than 1");
        }

        if (StringUtils.isNotBlank(templateFile)) {
            final File file = new File(templateFile);
            if (!file.exists()) {
                throw new ConfigException(SECTION, "Template file does not exist");
            }
        }
    }

    @Override
    public Boolean getEnabled() {
        return true;
    }

    private boolean differenceIsInvalid(final Double min, final Double max) {
        final double realMin = Optional.ofNullable(min).orElse(0D);
        final double realMax = Optional.ofNullable(max).orElse(0D);
        return realMin + realMax >= 1;
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class PowerPointConfigBuilder {
    }
}
