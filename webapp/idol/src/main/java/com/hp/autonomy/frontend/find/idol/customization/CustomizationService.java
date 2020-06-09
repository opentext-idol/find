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

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

interface CustomizationService {
    void saveAsset(final AssetType assetType, MultipartFile file, final boolean overwrite) throws CustomizationException;

    List<String> getAssets(final AssetType assetType) throws CustomizationException;

    File getAsset(final AssetType assetType, final String name);

    boolean deleteAsset(final AssetType assetType, final String name);
}
