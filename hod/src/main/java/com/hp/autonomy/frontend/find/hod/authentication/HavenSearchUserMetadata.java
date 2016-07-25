/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.authentication;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.NONE)
public class HavenSearchUserMetadata {

    public static final String USERNAME = "HAVEN_SEARCH_ONDEMAND_USERNAME";

    public static final Map<String, Class<? extends Serializable>> METADATA_TYPES = ImmutableMap.<String, Class<? extends Serializable>>builder()
            .put(USERNAME, String.class)
            .build();

}
