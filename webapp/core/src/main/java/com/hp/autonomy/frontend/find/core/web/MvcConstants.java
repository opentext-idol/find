/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

public enum MvcConstants {
    APPLICATION_PATH("applicationPath"),
    BASE_URL("baseUrl"),
    CONFIG("configJson"),
    GIT_COMMIT("commit"),
    MAIN_JS("mainJs"),
    MAP("map"),
    RELEASE_VERSION("version"),
    METRICS_ENABLED("metricsEnabled"),
    ROLES("roles"),
    SAVED_SEARCH_CONFIG("savedSearchConfig"),
    USERNAME("username"),
    MIN_SCORE("minScore"),
    UI_CUSTOMIZATION("uiCustomization"),
    FIELDS_INFO("fieldsInfo"),
    TOPIC_MAP_MAX_RESULTS("topicMapMaxResults"),
    METADATA_FIELD_IDS("metadataFieldIds"),
    ANSWER_SERVER_ENABLED("answerServerEnabled"),
    PUBLIC_INDEXES_ENABLED("publicIndexesEnabled");

    private final String value;

    MvcConstants(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
