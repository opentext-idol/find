/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

abstract class AbstractCustomizationController {
    private final CustomizationService customizationService;

    AbstractCustomizationController(final CustomizationService customizationService) {
        this.customizationService = customizationService;
    }

    protected ResponseEntity<Resource> logo(final AssetType assetType, final String name) {
        if(name == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final FileSystemResource fileSystemResource = new FileSystemResource(customizationService.getAsset(assetType, name));

        if(!fileSystemResource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(fileSystemResource, HttpStatus.OK);
    }
}
