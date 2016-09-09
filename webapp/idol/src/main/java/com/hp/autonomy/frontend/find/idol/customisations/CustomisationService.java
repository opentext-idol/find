/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

interface CustomisationService {

    void saveLogo(MultipartFile file, final boolean overwrite) throws CustomisationException;

    List<String> getLogos() throws CustomisationException;

    File getLogo(final String name);

    boolean deleteLogo(final String name);

}
