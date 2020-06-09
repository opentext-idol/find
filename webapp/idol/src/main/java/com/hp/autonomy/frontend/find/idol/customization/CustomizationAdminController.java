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
@RequestMapping({CustomizationReloadController.ADMIN_CUSTOMIZATION_PATH})
class CustomizationAdminController extends AbstractCustomizationController {
    static final String ASSETS_PATH = "/assets";
    static final String CONFIG_PATH = "/config";
    private static final String TYPED_ASSETS_PATH = ASSETS_PATH + "/{type}";

    private static final String ASSET_ID_PATH = TYPED_ASSETS_PATH + "/{name:.+}";

    private static final MediaType IMAGE_MEDIA_TYPE = new MediaType("image");
    private final CustomizationService customizationService;
    private final WriteableConfigService<AssetConfig> assetConfigService;

    @Autowired
    public CustomizationAdminController(
        final CustomizationService customizationService,
        final WriteableConfigService<AssetConfig> assetConfigService) {
        super(customizationService);

        this.customizationService = customizationService;
        this.assetConfigService = assetConfigService;
    }

    @RequestMapping(value = TYPED_ASSETS_PATH, method = RequestMethod.POST)
    public ResponseEntity<Status> postLogo(
        @PathVariable("type") final AssetType assetType,
        @RequestPart("file") final MultipartFile file
    ) throws CustomizationException {
        return saveLogo(assetType, file, false);
    }

    @RequestMapping(value = TYPED_ASSETS_PATH, method = RequestMethod.PUT)
    public ResponseEntity<Status> putLogo(
        @PathVariable("type") final AssetType assetType,
        @RequestPart("file") final MultipartFile file
    ) throws CustomizationException {
        return saveLogo(assetType, file, true);
    }

    @RequestMapping(value = TYPED_ASSETS_PATH, method = RequestMethod.GET)
    public List<String> logos(
        @PathVariable("type") final AssetType assetType
    ) throws CustomizationException {
        return customizationService.getAssets(assetType);
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
        customizationService.deleteAsset(assetType, name);
    }

    @RequestMapping(value = CONFIG_PATH, method = RequestMethod.POST)
    public ResponseEntity<?> updateConfig(
        @RequestBody final AssetConfig assetConfig
    ) throws Exception {
        try {
            assetConfigService.updateConfig(assetConfig);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch(final ConfigValidationException cve) {
            return new ResponseEntity<>(Collections.singletonMap("validation", cve.getValidationErrors()), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @ExceptionHandler(CustomizationException.class)
    public ResponseEntity<Status> handleException(final CustomizationException e) {
        return new ResponseEntity<>(e.getStatus(), e.getStatus().getHttpStatus());
    }

    private ResponseEntity<Status> saveLogo(final AssetType assetType, final MultipartFile file, final boolean overwrite) throws CustomizationException {
        if(file == null) {
            return new ResponseEntity<>(Status.INVALID_FILE, Status.INVALID_FILE.getHttpStatus());
        }

        final String contentType = file.getContentType();

        if(contentType == null || !IMAGE_MEDIA_TYPE.includes(MediaType.parseMediaType(contentType))) {
            return new ResponseEntity<>(Status.INVALID_FILE, Status.INVALID_FILE.getHttpStatus());
        }

        customizationService.saveAsset(assetType, file, overwrite);

        return new ResponseEntity<>(Status.SUCCESS, HttpStatus.CREATED);
    }
}
