/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

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
@RequestMapping("/api/public/customisation")
public class CustomisationController extends AbstractCustomisationController {

    private final ConfigService<AssetConfig> configService;

    @Autowired
    public CustomisationController(
            final CustomisationService customisationService,
            final ConfigService<AssetConfig> configService
    ) {
        super(customisationService);
        this.configService = configService;
    }

    @RequestMapping(value = "/logo/{type}/current", method = RequestMethod.GET)
    public ResponseEntity<Resource> logo(@PathVariable("type") final AssetType type) {
        ResponseEntity<Resource> logo = super.logo(type, configService.getConfig().getAssetPath(type));

        if (logo.getStatusCode() == HttpStatus.NOT_FOUND) {
            // nothing configured or configured file was not found
            logo = new ResponseEntity<>(new ClassPathResource(type.getDefaultValue()), HttpStatus.OK);
        }

        return logo;
    }

}
