/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization.style;

import com.hp.autonomy.frontend.find.core.customization.style.CssGenerationException;

import java.nio.file.Path;

public interface StyleCompiler {
    String compile(Path path) throws CssGenerationException;
}
