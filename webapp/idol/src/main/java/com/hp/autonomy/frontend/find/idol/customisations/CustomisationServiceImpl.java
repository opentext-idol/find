/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CustomisationServiceImpl implements CustomisationService {

    private static final String CUSTOMIZATIONS_DIRECTORY = "customizations";
    private static final String LOGO_DIRECTORY = "logos";

    private final String homeDirectory;

    @Autowired
    public CustomisationServiceImpl(@Value("${hp.find.home}") final String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    @Override
    public void saveLogo(final MultipartFile file, final boolean overwrite) throws CustomisationException {
        checkLogoDirectory();

        final File outputFile = getLogo(file.getOriginalFilename());

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
    public List<String> getLogos() throws CustomisationException {
        checkLogoDirectory();

        final File logoDirectory = new File(getLogoDirectoryPath());

        final String[] list = logoDirectory.list();

        if (list == null) {
            throw new CustomisationException(Status.DIRECTORY_ERROR, "Error listing customisations directory");
        }

        Arrays.sort(list);

        return Arrays.asList(list);
    }

    @Override
    public File getLogo(final String name) {
        return new File(getLogoDirectoryPath() + File.separator + name);
    }

    @Override
    public boolean deleteLogo(final String name) {
        return getLogo(name).delete();
    }

    private boolean ensureLogoDirectory() {
        final File logoDirectory = new File(getLogoDirectoryPath());

        if(!logoDirectory.exists()) {
            return logoDirectory.mkdirs();
        }
        else if (!logoDirectory.isDirectory()) {
            return false;
        }

        return true;
    }

    private void checkLogoDirectory() throws CustomisationException {
        if (!ensureLogoDirectory()) {
            throw new CustomisationException(Status.DIRECTORY_ERROR, "Error with customisations directory");
        }
    }

    private String getLogoDirectoryPath() {
        return homeDirectory + File.separator + CUSTOMIZATIONS_DIRECTORY + File.separator + LOGO_DIRECTORY;
    }

}
