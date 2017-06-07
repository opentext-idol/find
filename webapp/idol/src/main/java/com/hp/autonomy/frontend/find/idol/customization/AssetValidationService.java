/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.ValidationResults;
import com.hp.autonomy.frontend.configuration.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class AssetValidationService implements ValidationService<AssetConfig> {
    private final CustomizationService customizationService;

    @Autowired
    public AssetValidationService(final CustomizationService customizationService) {
        this.customizationService = customizationService;
    }

    @Override
    public ValidationResults validateConfig(final AssetConfig config) {
        final ValidationResults.Builder builder = new ValidationResults.Builder();

        Arrays.stream(AssetType.values())
            .collect(Collectors.toMap(
                Enum::name,
                type -> {
                    final String assetPath = config.getAssetPath(type);

                    if(assetPath == null) {
                        return new ValidationResult<>(true);
                    }

                    final File asset = customizationService.getAsset(type, assetPath);

                    return new ValidationResult<>(asset.exists());
                }))
            .forEach(builder::put);

        return builder.build();
    }

    @Override
    public ValidationResults validateEnabledConfig(final AssetConfig config) {
        return validateConfig(config);
    }

    @Override
    public ValidationResult<?> validate(final OptionalConfigurationComponent<?> configurationComponent) {
        throw new UnsupportedOperationException("This service doesn't support component validation");
    }
}
