/*
 * Copyright 2016-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

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

import static com.hp.autonomy.frontend.find.core.configuration.FindConfigFileService.CONFIG_FILE_LOCATION_SPEL;

@Slf4j
@Service
public class CustomizationServiceImpl implements CustomizationService {
    private static final String CUSTOMIZATIONS_DIRECTORY = CustomizationConfigService.CONFIG_DIRECTORY;
    private static final String ASSETS_DIRECTORY = "assets";

    private final String homeDirectory;

    @Autowired
    public CustomizationServiceImpl(@Value(CONFIG_FILE_LOCATION_SPEL) final String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    @Override
    public void saveAsset(final AssetType assetType, final MultipartFile file, final boolean overwrite) throws CustomizationException {
        checkAssetsDirectory(assetType);

        final File outputFile = getAsset(assetType, file.getOriginalFilename());

        if(!overwrite && outputFile.exists()) {
            throw new CustomizationException(Status.FILE_EXISTS, "Logo file already exists");
        }

        try {
            file.transferTo(outputFile);
        } catch(final IOException e) {
            log.error("Error writing file", e);

            throw new CustomizationException(Status.IO_ERROR, "Error writing logo file");
        }
    }

    @Override
    public List<String> getAssets(final AssetType assetType) throws CustomizationException {
        checkAssetsDirectory(assetType);

        final File assetsDirectory = new File(getAssetDirectoryPath(assetType));

        final String[] list = assetsDirectory.list();

        if(list == null) {
            throw new CustomizationException(Status.DIRECTORY_ERROR, "Error listing customizations directory");
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
        if(name == null) {
            return false;
        }

        return getAsset(assetType, name).delete();
    }

    private boolean ensureAssetsDirectory(final AssetType assetType) {
        final File assetsDirectory = new File(getAssetDirectoryPath(assetType));

        if(!assetsDirectory.exists()) {
            return assetsDirectory.mkdirs();
        } else if(!assetsDirectory.isDirectory()) {
            return false;
        }

        return true;
    }

    private void checkAssetsDirectory(final AssetType assetType) throws CustomizationException {
        if(!ensureAssetsDirectory(assetType)) {
            throw new CustomizationException(Status.DIRECTORY_ERROR, "Error with customizations directory");
        }
    }

    private String getAssetDirectoryPath(final AssetType assetType) {
        return homeDirectory + File.separator + CUSTOMIZATIONS_DIRECTORY + File.separator + ASSETS_DIRECTORY + File.separator + assetType.getDirectory();
    }
}
