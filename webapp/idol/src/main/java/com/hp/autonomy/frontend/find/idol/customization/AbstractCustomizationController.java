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
