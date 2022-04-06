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

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.configuration.validation.ValidationService;
import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetConfigService extends CustomizationConfigService<AssetConfig> {
    @Autowired
    protected AssetConfigService(final ValidationService<AssetConfig> validationService) {
        super(
            "assets.json",
            "defaultAssetsConfigFile.json",
            AssetConfig.class,
            AssetConfig.builder().build()
        );

        setValidationService(validationService);
    }
}
