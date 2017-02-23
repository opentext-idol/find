/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;

/**
 * Builder for configuration common to both HoD and Idol implementations of Find
 *
 * @param <C> configuration implementation
 * @param <B> configuration implementation builder
 */
@SuppressWarnings("unused")
public interface FindConfigBuilder<C extends FindConfig<C, B>, B extends FindConfigBuilder<C, B>> {
    B map(MapConfiguration mapConfiguration);

    B savedSearchConfig(SavedSearchConfig savedSearchConfig);

    B minScore(Integer minScore);

    B fieldsInfo(FieldsInfo fieldsInfo);

    B uiCustomization(UiCustomization uiCustomization);

    B topicMapMaxResults(Integer topicMapMaxResults);

    C build();
}
