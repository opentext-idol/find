/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

abstract class AbstractCustomisationController {

    private final CustomisationService customisationService;

    AbstractCustomisationController(final CustomisationService customisationService) {
        this.customisationService = customisationService;
    }

    protected ResponseEntity<FileSystemResource> logo(final AssetType assetType, final String name) {
        final FileSystemResource fileSystemResource = new FileSystemResource(customisationService.getAsset(assetType, name));

        if (!fileSystemResource.exists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(fileSystemResource, HttpStatus.OK);
    }

}
