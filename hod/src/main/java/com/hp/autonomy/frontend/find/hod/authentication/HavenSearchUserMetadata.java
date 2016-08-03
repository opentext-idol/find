/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.authentication;

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Map;

public interface HavenSearchUserMetadata {
    String LEGACY_USER_DISPLAY_NAME = "HAVEN_SEARCH_ONDEMAND_USERNAME";
    String USER_DISPLAY_NAME = "DisplayName";

    Map<String, Class<? extends Serializable>> METADATA_TYPES = ImmutableMap.<String, Class<? extends Serializable>>builder()
            .put(LEGACY_USER_DISPLAY_NAME, String.class)
            .put(USER_DISPLAY_NAME, String.class)
            .build();
}
