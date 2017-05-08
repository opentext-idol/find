package com.hp.autonomy.frontend.find.core.configuration.export;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PowerPointConfigValidator implements Validator<PowerPointConfig> {
    private final PowerPointService powerPointService;

    @Autowired
    public PowerPointConfigValidator(final PowerPointService powerPointService) {
        this.powerPointService = powerPointService;
    }

    @Override
    public ValidationResult<?> validate(final PowerPointConfig powerPointConfig) {
        try {
            powerPointConfig.basicValidate(null);
        } catch (final ConfigException ignored) {
            return new ValidationResult<>(false, Validation.CONFIGURATION_INVALID);
        }

        final String templateFile = powerPointConfig.getTemplateFile();
        if (StringUtils.isNotBlank(templateFile)) {
            try {
                powerPointService.validateTemplate();
            } catch (final TemplateLoadException ignored) {
                return new ValidationResult<>(false, Validation.TEMPLATE_INVALID);
            }
        }

        return new ValidationResult<>(true, null);
    }

    @Override
    public Class<PowerPointConfig> getSupportedClass() {
        return PowerPointConfig.class;
    }

    private enum Validation {
        CONFIGURATION_INVALID,
        TEMPLATE_INVALID
    }
}
