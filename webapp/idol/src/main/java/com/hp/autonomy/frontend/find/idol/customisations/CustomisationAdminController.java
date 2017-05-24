/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import com.hp.autonomy.frontend.configuration.WriteableConfigService;
import com.hp.autonomy.frontend.configuration.validation.ConfigValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping({CustomisationAdminController.CUSTOMISATIONS_PATH})
class CustomisationAdminController extends AbstractCustomisationController {

    static final String CUSTOMISATIONS_PATH = "/api/admin/customisation";
    static final String ASSETS_PATH = "/assets";
    private static final String TYPED_ASSETS_PATH = ASSETS_PATH + "/{type}";
    private static final String ASSET_ID_PATH = TYPED_ASSETS_PATH + "/{name:.+}";

    private static final MediaType IMAGE_MEDIA_TYPE = new MediaType("image");

    private final CustomisationService customisationService;
    private final WriteableConfigService<AssetConfig> assetConfigService;

    @Autowired
    public CustomisationAdminController(
            final CustomisationService customisationService,
            final WriteableConfigService<AssetConfig> assetConfigService) {
        super(customisationService);

        this.customisationService = customisationService;
        this.assetConfigService = assetConfigService;
    }

    @RequestMapping(value = TYPED_ASSETS_PATH, method = RequestMethod.POST)
    public ResponseEntity<Status> postLogo(
        @PathVariable("type") final AssetType assetType,
        @RequestPart("file") final MultipartFile file
    ) throws CustomisationException {
        return saveLogo(assetType, file, false);
    }

    @RequestMapping(value = TYPED_ASSETS_PATH, method = RequestMethod.PUT)
    public ResponseEntity<Status> putLogo(
        @PathVariable("type") final AssetType assetType,
        @RequestPart("file") final MultipartFile file
    ) throws CustomisationException {
        return saveLogo(assetType, file, true);
    }

    @RequestMapping(value = TYPED_ASSETS_PATH, method = RequestMethod.GET)
    public List<String> logos(
        @PathVariable("type") final AssetType assetType
    ) throws CustomisationException {
        return customisationService.getAssets(assetType);
    }

    @Override
    @RequestMapping(value = ASSET_ID_PATH, method = RequestMethod.GET)
    public ResponseEntity<Resource> logo(
        @PathVariable("type") final AssetType assetType,
        @PathVariable("name") final String name
    ) {
        return super.logo(assetType, name);
    }

    @RequestMapping(value = ASSET_ID_PATH, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLogo(
        @PathVariable("type") final AssetType assetType,
        @PathVariable("name") final String name
    ) {
        customisationService.deleteAsset(assetType, name);
    }

    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public ResponseEntity<?> updateConfig(
            @RequestBody final AssetConfig assetConfig
    ) throws Exception {
        try {
            assetConfigService.updateConfig(assetConfig);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (final ConfigValidationException cve) {
            return new ResponseEntity<>(Collections.singletonMap("validation", cve.getValidationErrors()), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @ExceptionHandler(CustomisationException.class)
    public ResponseEntity<Status> handleException(final CustomisationException e) {
        return new ResponseEntity<>(e.getStatus(), e.getStatus().getHttpStatus());
    }

    private ResponseEntity<Status> saveLogo(final AssetType assetType, final MultipartFile file, final boolean overwrite) throws CustomisationException {
        if (file == null) {
            return new ResponseEntity<>(Status.INVALID_FILE, Status.INVALID_FILE.getHttpStatus());
        }

        final String contentType = file.getContentType();

        if (contentType == null || !IMAGE_MEDIA_TYPE.includes(MediaType.parseMediaType(contentType))) {
            return new ResponseEntity<>(Status.INVALID_FILE, Status.INVALID_FILE.getHttpStatus());
        }

        customisationService.saveAsset(assetType, file, overwrite);

        return new ResponseEntity<>(Status.SUCCESS, HttpStatus.CREATED);
    }

}
