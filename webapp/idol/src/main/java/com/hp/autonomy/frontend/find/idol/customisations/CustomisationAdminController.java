/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping({CustomisationAdminController.CUSTOMISATIONS_PATH})
class CustomisationAdminController extends AbstractCustomisationController {

    static final String CUSTOMISATIONS_PATH = "/api/admin/customisation";
    static final String LOGO_PATH = "/logo";
    private static final String LOGO_ID_PATH = LOGO_PATH + "/{name:.+}";

    private static final MediaType IMAGE_MEDIA_TYPE = new MediaType("image");

    private final CustomisationService customisationService;

    @Autowired
    public CustomisationAdminController(final CustomisationService customisationService) {
        super(customisationService);

        this.customisationService = customisationService;
    }

    @RequestMapping(value = LOGO_PATH, method = RequestMethod.POST)
    public ResponseEntity<Status> postLogo(
        @RequestPart("file") final MultipartFile file
    ) throws CustomisationException {
        return saveLogo(file, false);
    }

    @RequestMapping(value = LOGO_PATH, method = RequestMethod.PUT)
    public ResponseEntity<Status> putLogo(
        @RequestPart("file") final MultipartFile file
    ) throws CustomisationException {
        return saveLogo(file, true);
    }

    @RequestMapping(value = LOGO_PATH, method = RequestMethod.GET)
    public List<String> logos() throws CustomisationException {
        return customisationService.getLogos();
    }

    @Override
    @RequestMapping(value = LOGO_ID_PATH, method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> logo(
        @PathVariable("name") final String name
    ) {
        return super.logo(name);
    }

    @RequestMapping(value = LOGO_ID_PATH, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLogo(
        @PathVariable("name") final String name
    ) {
        customisationService.deleteLogo(name);
    }

    @ExceptionHandler(CustomisationException.class)
    public ResponseEntity<Status> handleException(final CustomisationException e) {
        return new ResponseEntity<>(e.getStatus(), e.getStatus().getHttpStatus());
    }

    private ResponseEntity<Status> saveLogo(final MultipartFile file, final boolean overwrite) throws CustomisationException {
        if (file == null) {
            return new ResponseEntity<>(Status.INVALID_FILE, Status.INVALID_FILE.getHttpStatus());
        }

        final String contentType = file.getContentType();

        if (contentType == null || !IMAGE_MEDIA_TYPE.includes(MediaType.parseMediaType(contentType))) {
            return new ResponseEntity<>(Status.INVALID_FILE, Status.INVALID_FILE.getHttpStatus());
        }

        customisationService.saveLogo(file, overwrite);

        return new ResponseEntity<>(Status.SUCCESS, HttpStatus.CREATED);
    }

}
