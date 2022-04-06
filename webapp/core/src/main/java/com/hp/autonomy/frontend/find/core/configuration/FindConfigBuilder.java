/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.find.core.configuration.export.ExportConfig;
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

    B export(ExportConfig exportConfig);

    C build();
}
