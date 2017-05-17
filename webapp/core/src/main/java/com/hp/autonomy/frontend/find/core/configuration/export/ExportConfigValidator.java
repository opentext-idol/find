package com.hp.autonomy.frontend.find.core.configuration.export;

import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExportConfigValidator implements Validator<ExportConfig> {
    private final Validator<PowerPointConfig> powerPointConfigValidator;

    @Autowired
    public ExportConfigValidator(final Validator<PowerPointConfig> powerPointConfigValidator) {
        this.powerPointConfigValidator = powerPointConfigValidator;
    }

    @Override
    public ValidationResult<?> validate(final ExportConfig config) {
        return Optional.ofNullable(config.getPowerpoint())
                .map(powerPointConfigValidator::validate)
                .orElse(new ValidationResult<>(true, null));
    }

    @Override
    public Class<ExportConfig> getSupportedClass() {
        return ExportConfig.class;
    }
}
