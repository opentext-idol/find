/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization.style;

import java.util.Optional;

public interface StyleSheetService {

    void generateCss() throws CssGenerationException;

    Optional<StyleSheet> getCss(String fileName);

}
