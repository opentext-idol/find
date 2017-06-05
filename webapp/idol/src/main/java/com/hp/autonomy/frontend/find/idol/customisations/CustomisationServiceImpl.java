/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import com.hp.autonomy.frontend.find.core.configuration.CustomizationConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CustomisationServiceImpl implements CustomisationService {

    private static final String CUSTOMIZATIONS_DIRECTORY = CustomizationConfigService.CONFIG_DIRECTORY;
    private static final String ASSETS_DIRECTORY = "assets";

    private final String homeDirectory;

    @Autowired
    public CustomisationServiceImpl(@Value("${hp.find.home}") final String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    @Override
    public void saveAsset(final AssetType assetType, final MultipartFile file, final boolean overwrite) throws CustomisationException {
        checkAssetsDirectory(assetType);

        final File outputFile = getAsset(assetType, file.getOriginalFilename());

        if (!overwrite && outputFile.exists()) {
            throw new CustomisationException(Status.FILE_EXISTS, "Logo file already exists");
        }

        try {
            file.transferTo(outputFile);
        }
        catch (final IOException e) {
            log.error("Error writing file", e);

            throw new CustomisationException(Status.IO_ERROR, "Error writing logo file");
        }
    }

    @Override
    public List<String> getAssets(final AssetType assetType) throws CustomisationException {
        checkAssetsDirectory(assetType);

        final File assetsDirectory = new File(getAssetDirectoryPath(assetType));

        final String[] list = assetsDirectory.list();

        if (list == null) {
            throw new CustomisationException(Status.DIRECTORY_ERROR, "Error listing customisations directory");
        }

        Arrays.sort(list);

        return Arrays.asList(list);
    }

    @Override
    public File getAsset(final AssetType assetType, final String name) {
        return new File(getAssetDirectoryPath(assetType) + File.separator + name);
    }

    @Override
    public boolean deleteAsset(final AssetType assetType, final String name) {
        if (name == null) {
            return false;
        }

        return getAsset(assetType, name).delete();
    }

    private boolean ensureAssetsDirectory(final AssetType assetType) {
        final File assetsDirectory = new File(getAssetDirectoryPath(assetType));

        if(!assetsDirectory.exists()) {
            return assetsDirectory.mkdirs();
        }
        else if (!assetsDirectory.isDirectory()) {
            return false;
        }

        return true;
    }

    private void checkAssetsDirectory(final AssetType assetType) throws CustomisationException {
        if (!ensureAssetsDirectory(assetType)) {
            throw new CustomisationException(Status.DIRECTORY_ERROR, "Error with customisations directory");
        }
    }

    private String getAssetDirectoryPath(final AssetType assetType) {
        return homeDirectory + File.separator + CUSTOMIZATIONS_DIRECTORY + File.separator + ASSETS_DIRECTORY + File.separator + assetType.getDirectory();
    }

}
