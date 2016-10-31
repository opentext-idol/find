/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// The properties configured in this class can't be set in a standard "Spring Boot" manner, only through system properties.
@NoArgsConstructor(access = AccessLevel.NONE)
public class TomcatSettings {
    private static final String ALLOW_ENCODED_SLASHES_PROPERTY = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";

    /**
     * Apply global Tomcat settings required for the application to run correctly.
     */
    public static void apply() {
        // By default, Tomcat does not allow encoded slashes in URLs to prevent directory traversal attacks.
        // Find has nothing which can be attacked in this way, so it is safe to remove this.
        if (System.getProperty(ALLOW_ENCODED_SLASHES_PROPERTY) == null) {
            System.setProperty(ALLOW_ENCODED_SLASHES_PROPERTY, "true");
        }
    }
}
