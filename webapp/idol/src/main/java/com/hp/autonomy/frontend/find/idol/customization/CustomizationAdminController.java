/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import com.hp.autonomy.frontend.configuration.WriteableConfigService;
import com.hp.autonomy.frontend.configuration.validation.ConfigValidationException;
import com.hp.autonomy.frontend.find.idol.api.CustomizationAdminApi;
import com.hp.autonomy.frontend.find.idol.api.model.AssetsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Slf4j
class CustomizationAdminController extends AbstractCustomizationController implements CustomizationAdminApi {
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

    @Override
    public ResponseEntity<Status> createAsset(final AssetType type, final MultipartFile file) {
        return saveLogo(type, file, false);
    }

    @Override
    public ResponseEntity<Status> replaceAsset(final AssetType type, final MultipartFile file) {
        return saveLogo(type, file, true);
    }

    @Override
    public ResponseEntity<AssetsResponse> getAssets(final AssetType type) {
        return ResponseEntity.ok(new AssetsResponse(customizationService.getAssets(type)));
    }

    @Override
    public ResponseEntity<Resource> getAsset(final AssetType type, final String name) {
        return super.logo(type, name);
    }

    @Override
    public ResponseEntity<Void> deleteAsset(final AssetType type, final String name) {
        customizationService.deleteAsset(type, name);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateCustomizationConfig(final AssetConfig assetConfig) {
        try {
            assetConfigService.updateConfig(assetConfig);
        } catch(final ConfigValidationException cve) {
            throw new ConfigValidationRuntimeException(cve);
        } catch(final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CustomizationException.class)
    public ResponseEntity<Status> handleException(final CustomizationException e) {
        return new ResponseEntity<>(e.getStatus(), e.getStatus().getHttpStatus());
    }

    @ExceptionHandler(ConfigValidationRuntimeException.class)
    public ResponseEntity<Object> handleConfigValidationException(final ConfigValidationRuntimeException e) {
        return new ResponseEntity<>(
            Map.of("validation", e.getValidationException().getValidationErrors()),
            HttpStatus.NOT_ACCEPTABLE);
    }

    private ResponseEntity<Status> saveLogo(final AssetType assetType, final MultipartFile file, final boolean overwrite) {
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
