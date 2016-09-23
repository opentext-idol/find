/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

interface CustomisationService {

    void saveAsset(final AssetType assetType, MultipartFile file, final boolean overwrite) throws CustomisationException;

    List<String> getAssets(final AssetType assetType) throws CustomisationException;

    File getAsset(final AssetType assetType, final String name);

    boolean deleteAsset(final AssetType assetType, final String name);

}
