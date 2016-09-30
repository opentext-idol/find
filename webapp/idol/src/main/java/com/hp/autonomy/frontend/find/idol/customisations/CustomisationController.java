/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/customisation")
public class CustomisationController extends AbstractCustomisationController {

    @Autowired
    public CustomisationController(final CustomisationService customisationService) {
        super(customisationService);
    }

    @RequestMapping(value = "/logo/{type}/current", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> logo(@PathVariable("type") final AssetType type) {
        // TODO get current logo from config file
        return super.logo(null, "");
    }

}
