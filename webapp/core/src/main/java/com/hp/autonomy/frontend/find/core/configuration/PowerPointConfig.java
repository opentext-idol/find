/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointServiceImpl;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateSettingsSource;
import com.hp.autonomy.frontend.reports.powerpoint.dto.Anchor;
import java.io.File;
import java.io.FileInputStream;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

import static com.hp.autonomy.frontend.find.core.configuration.PowerPointConfig.Validation.INVALID_MARGINS;

@JsonDeserialize(builder = PowerPointConfig.Builder.class)
@Data
public class PowerPointConfig implements OptionalConfigurationComponent<PowerPointConfig> {
    private final String templateFile;

    private final Double marginTop, marginLeft, marginRight, marginBottom;

    private PowerPointConfig(final Builder builder) {
        templateFile = builder.templateFile;
        marginTop = builder.marginTop;
        marginLeft = builder.marginLeft;
        marginRight = builder.marginRight;
        marginBottom = builder.marginBottom;
    }

    @Override
    public PowerPointConfig merge(final PowerPointConfig savedSearchConfig) {
        return savedSearchConfig != null ?
                new PowerPointConfig.Builder()
                        .setTemplateFile(templateFile == null ? savedSearchConfig.templateFile : templateFile)
                        .setMarginTop(marginTop == null ? savedSearchConfig.marginTop : marginTop)
                        .setMarginLeft(marginLeft == null ? savedSearchConfig.marginLeft : marginLeft)
                        .setMarginRight(marginRight == null ? savedSearchConfig.marginRight : marginRight)
                        .setMarginBottom(marginBottom == null ? savedSearchConfig.marginBottom : marginBottom)
                        .build()
                : this;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        final ValidationResult<?> result = validate();

        if (!result.isValid()) {
            throw new ConfigException(section, String.valueOf(result.getData()));
        }
    }

    @JsonIgnore
    public Anchor getAnchor() {
        final Anchor anchor = new Anchor();

        final double tmpMarginTop = marginTop == null ? 0 : marginTop;
        final double tmpMarginLeft = marginLeft == null ? 0 : marginLeft;
        final double tmpMarginRight = marginRight == null ? 0 : marginRight;
        final double tmpMarginBottom = marginBottom == null ? 0 : marginBottom;

        anchor.setX(tmpMarginLeft);
        anchor.setY(tmpMarginTop);
        anchor.setWidth(1 - tmpMarginLeft - tmpMarginRight);
        anchor.setHeight(1 - tmpMarginTop - tmpMarginBottom);

        return anchor;
    }

    public ValidationResult<Validation> validate() {
        if(rangeIsInvalid(marginTop)) {
            return new ValidationResult<>(false, INVALID_MARGINS);
        }
        if(rangeIsInvalid(marginLeft)) {
            return new ValidationResult<>(false, INVALID_MARGINS);
        }
        if(rangeIsInvalid(marginRight)) {
            return new ValidationResult<>(false, INVALID_MARGINS);
        }
        if(rangeIsInvalid(marginBottom)) {
            return new ValidationResult<>(false, INVALID_MARGINS);
        }
        if(differenceIsInvalid(marginLeft, marginRight)) {
            return new ValidationResult<>(false, INVALID_MARGINS);
        }
        if(differenceIsInvalid(marginTop, marginBottom)) {
            return new ValidationResult<>(false, INVALID_MARGINS);
        }

        if(StringUtils.isNotBlank(templateFile)) {
            final File file = new File(templateFile);

            if (!file.exists()) {
                return new ValidationResult<>(false, Validation.TEMPLATE_FILE_NOT_FOUND);
            }

            final PowerPointServiceImpl service = new PowerPointServiceImpl(() -> new FileInputStream(file), TemplateSettingsSource.DEFAULT);

            try {
                service.validateTemplate();
            }
            catch(TemplateLoadException e) {
                return new ValidationResult<>(false, Validation.TEMPLATE_INVALID);
            }
        }

        return new ValidationResult<>(true, null);
    }

    private boolean differenceIsInvalid(final Double min, final Double max) {
        double realMin = min == null ? 0 : min;
        double realMax = max == null ? 0 : max;
        if (realMin + realMax >= 1) {
            return true;
        }
        return false;
    }

    private static boolean rangeIsInvalid(final Double value) {
        return value != null && (value >= 1 || value < 0);
    }

    @Override
    public Boolean getEnabled() {
        return true;
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String templateFile;
        private Double marginTop, marginLeft, marginRight, marginBottom;

        public PowerPointConfig build() {
            return new PowerPointConfig(this);
        }
    }

    public enum Validation {
        TEMPLATE_FILE_NOT_FOUND,
        TEMPLATE_INVALID,
        INVALID_MARGINS;

        private Validation() {
        }
    }
}
