/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

interface CustomizationService {
    void saveAsset(final AssetType assetType, MultipartFile file, final boolean overwrite) throws CustomizationException;

    List<String> getAssets(final AssetType assetType) throws CustomizationException;

    File getAsset(final AssetType assetType, final String name);

    boolean deleteAsset(final AssetType assetType, final String name);
}
