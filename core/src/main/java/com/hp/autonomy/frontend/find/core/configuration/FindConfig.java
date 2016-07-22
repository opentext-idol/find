/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;

import java.util.Set;

public interface FindConfig {

    MapConfiguration getMap();

    SavedSearchConfig getSavedSearchConfig();

    Integer getMinScore();

    FieldsInfo getFieldsInfo();

    Boolean getDirectAccessLink();

    Set<ParametricDisplayValues> getParametricDisplayValues();

}
