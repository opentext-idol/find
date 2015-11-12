package com.hp.autonomy.frontend.find.authentication;

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
