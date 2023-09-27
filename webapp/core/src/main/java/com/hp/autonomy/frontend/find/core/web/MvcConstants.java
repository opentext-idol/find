/*
 * Copyright 2015-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.web;

public enum MvcConstants {
    APPLICATION_PATH("applicationPath"),
    BASE_URL("baseUrl"),
    CONFIG("configJson"),
    GIT_COMMIT("commit"),
    MAIN_JS("mainJs"),
    SUNBURST("sunburst"),
    MAP("map"),
    RELEASE_VERSION("version"),
    ROLES("roles"),
    SAVED_SEARCH_CONFIG("savedSearchConfig"),
    USERNAME("username"),
    USERLABEL("userLabel"),
    MIN_SCORE("minScore"),
    UI_CUSTOMIZATION("uiCustomization"),
    FIELDS_INFO("fieldsInfo"),
    TOPIC_MAP_MAX_RESULTS("topicMapMaxResults"),
    METADATA_FIELD_INFO("metadataFieldInfo"),
    ANSWER_SERVER_ENABLED("answerServerEnabled"),
    CONVERSATION_ENABLED("conversationEnabled"),
    ENTITY_SEARCH_ENABLED("entitySearchEnabled"),
    ENTITY_SEARCH_OPTIONS("entitySearchOptions"),
    ENTITY_SEARCH_ANSWER_SERVER_ENABLED("entitySearchAnswerServerEnabled"),
    PUBLIC_INDEXES_ENABLED("publicIndexesEnabled"),
    CONTROL_POINT_ENABLED("controlPointEnabled"),
    NIFI_ENABLED("nifiEnabled"),
    TEMPLATES_CONFIG("templatesConfig"),
    ASSETS_CONFIG("assetsConfig"),
    MESSAGE_OF_THE_DAY_CONFIG("messageOfTheDay"),
    TERM_HIGHLIGHT_COLOR("termHighlightColor"),
    TERM_HIGHLIGHT_BACKGROUND("termHighlightBackground"),
    SEARCH_CONFIG("search"),
    RELATED_USERS_CONFIG("relatedUsers");

    private final String value;

    MvcConstants(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
