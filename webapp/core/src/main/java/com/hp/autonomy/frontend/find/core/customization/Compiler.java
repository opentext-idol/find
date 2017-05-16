/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization;

import java.nio.file.Path;

public interface Compiler {
    String compile(Path path) throws CssGenerationException;
}
