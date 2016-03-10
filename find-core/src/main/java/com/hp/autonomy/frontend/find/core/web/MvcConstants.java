/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

public enum MvcConstants {
    CONFIG("configJson"),
    GIT_COMMIT("commit"),
    MAIN_JS("mainJs"),
    RELEASE_VERSION("version"),
    USERNAME("username"),
    MAP("map");

    private final String value;

    MvcConstants(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
