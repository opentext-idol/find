/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointServiceImpl;
import com.hp.autonomy.frontend.reports.powerpoint.SlideShowTemplate;
import java.io.File;
import java.io.FileInputStream;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;

@JsonDeserialize(builder = PowerPointConfig.Builder.class)
@Data
public class PowerPointConfig implements OptionalConfigurationComponent<PowerPointConfig> {
    private final String templateFile;

    private PowerPointConfig(final Builder builder) {
        templateFile = builder.templateFile;
    }

    @Override
    public PowerPointConfig merge(final PowerPointConfig savedSearchConfig) {
        return savedSearchConfig != null ?
                new PowerPointConfig.Builder()
                        .setTemplateFile(templateFile == null ? savedSearchConfig.templateFile : templateFile)
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

    public ValidationResult<Validation> validate() {
        if(StringUtils.isNotBlank(templateFile)) {
            final File file = new File(templateFile);

            if (!file.exists()) {
                return new ValidationResult<>(false, Validation.TEMPLATE_FILE_NOT_FOUND);
            }

            final PowerPointServiceImpl service = new PowerPointServiceImpl(() -> new FileInputStream(file));

            try {
                service.validateTemplate();
            }
            catch(SlideShowTemplate.LoadException e) {
                return new ValidationResult<>(false, Validation.TEMPLATE_INVALID);
            }
        }

        return new ValidationResult<>(true, null);
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

        public PowerPointConfig build() {
            return new PowerPointConfig(this);
        }
    }

    public enum Validation {
        TEMPLATE_FILE_NOT_FOUND,
        TEMPLATE_INVALID;

        private Validation() {
        }
    }
}
