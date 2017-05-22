/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization.style;

public class CssGenerationException extends Exception {
    public CssGenerationException(final String s) {
        super(s);
    }

    public CssGenerationException(final String s, final Exception e) {
        super(s, e);
    }
}
