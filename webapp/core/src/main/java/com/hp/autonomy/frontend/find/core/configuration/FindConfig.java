/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.Config;
import com.hp.autonomy.frontend.configuration.authentication.AuthenticationConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;

import java.util.Collection;

/**
 * Configuration common to both HoD and Idol implementations of Find
 *
 * @param <C> configuration implementation
 * @param <B> configuration implementation builder
 */
public interface FindConfig<C extends FindConfig<C, B>, B extends FindConfigBuilder<C, B>> extends Config<C>, AuthenticationConfig<C> {
    MapConfiguration getMap();

    SavedSearchConfig getSavedSearchConfig();

    Integer getMinScore();

    FieldsInfo getFieldsInfo();

    UiCustomization getUiCustomization();

    Collection<ParametricDisplayValues> getParametricDisplayValues();

    Integer getTopicMapMaxResults();

    B toBuilder();

    PowerPointConfig getPowerPoint();
}
