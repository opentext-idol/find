/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

import com.hp.autonomy.frontend.configuration.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CustomizationController.PUBLIC_CUSTOMIZATION_PATH)
public class CustomizationController extends AbstractCustomizationController {
    static final String PUBLIC_CUSTOMIZATION_PATH = "/api/public/customization";
    private final ConfigService<AssetConfig> configService;

    @Autowired
    public CustomizationController(
        final CustomizationService customizationService,
        final ConfigService<AssetConfig> configService
    ) {
        super(customizationService);
        this.configService = configService;
    }

    @RequestMapping(value = "/logo/{type}/current", method = RequestMethod.GET)
    public ResponseEntity<Resource> logo(@PathVariable("type") final AssetType type) {
        ResponseEntity<Resource> logo = super.logo(type, configService.getConfig().getAssetPath(type));

        if(logo.getStatusCode() == HttpStatus.NOT_FOUND) {
            // nothing configured or configured file was not found
            logo = new ResponseEntity<>(new ClassPathResource(type.getDefaultValue()), HttpStatus.OK);
        }

        return logo;
    }
}
