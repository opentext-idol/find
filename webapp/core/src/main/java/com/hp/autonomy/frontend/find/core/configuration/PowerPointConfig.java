/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
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
public class PowerPointConfig implements ConfigurationComponent<PowerPointConfig> {
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
        if(StringUtils.isNotBlank(templateFile)) {
            final File file = new File(templateFile);

            if (!file.exists()) {
                throw new ConfigException(section, "Template file does not exist");
            }

            final PowerPointServiceImpl service = new PowerPointServiceImpl(() -> new FileInputStream(file));

            try {
                service.validateTemplate();
            }
            catch(SlideShowTemplate.LoadException e) {
                throw new ConfigException(section, e.getMessage());
            }
        }
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
}
