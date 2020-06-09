/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Builder(toBuilder = true)
@Getter
@JsonDeserialize(builder = UiCustomization.UiCustomizationBuilder.class)
public class UiCustomization implements ConfigurationComponent<UiCustomization> {
    private final UiCustomizationOptions options;
    private final Collection<String> filterOrder;
    @Singular("defaultDeselectedDatabase")
    private final Collection<String> defaultDeselectedDatabases;
    @Singular("parametricNeverShowItem")
    private final Collection<FieldPath> parametricNeverShow;
    @Singular("parametricAlwaysShowItem")
    private final Collection<FieldPath> parametricAlwaysShow;
    @Singular("parametricOrderItem")
    private final Collection<FieldPath> parametricOrder;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<String, String> specialUrlPrefixes;
    private final Map<String, String> previewWhitelistUrls;
    private final String errorCallSupportString;
    private final Boolean openSharedDashboardQueryAsNewSearch;
    private final SortParam parametricValuesSort;
    private final ProfileOptions profile;
    private final Integer listViewPagingSize;

    @Override
    public UiCustomization merge(final UiCustomization uiCustomization) {
        if(uiCustomization == null) {
            return this;
        } else {
            final Map<String, String> specialUrlPrefixes = new HashMap<>(uiCustomization.specialUrlPrefixes);
            specialUrlPrefixes.putAll(this.specialUrlPrefixes);
            return builder()
                .options(Optional.ofNullable(options).map(o -> o.merge(uiCustomization.options)).orElse(uiCustomization.options))
                .parametricNeverShow(CollectionUtils.isNotEmpty(parametricNeverShow)
                                         ? parametricNeverShow
                                         : uiCustomization.parametricNeverShow)
                .parametricAlwaysShow(CollectionUtils.isNotEmpty(parametricAlwaysShow)
                                          ? parametricAlwaysShow
                                          : uiCustomization.parametricAlwaysShow)
                .parametricOrder(CollectionUtils.isNotEmpty(parametricOrder)
                                     ? parametricOrder
                                     : uiCustomization.parametricOrder)
                .specialUrlPrefixes(specialUrlPrefixes)
                .previewWhitelistUrls(previewWhitelistUrls != null && !previewWhitelistUrls.isEmpty()
                                     ? previewWhitelistUrls :
                                     uiCustomization.previewWhitelistUrls)
                .errorCallSupportString(errorCallSupportString != null ? errorCallSupportString : uiCustomization.errorCallSupportString)
                .parametricValuesSort(parametricValuesSort != null ? parametricValuesSort : uiCustomization.parametricValuesSort)
                .defaultDeselectedDatabases(CollectionUtils.isNotEmpty(defaultDeselectedDatabases)
                                            ? defaultDeselectedDatabases
                                            : uiCustomization.defaultDeselectedDatabases)
                .filterOrder(CollectionUtils.isNotEmpty(filterOrder)
                                            ? filterOrder
                                            : uiCustomization.filterOrder)
                .openSharedDashboardQueryAsNewSearch(openSharedDashboardQueryAsNewSearch != null
                                            ? openSharedDashboardQueryAsNewSearch
                                            : uiCustomization.openSharedDashboardQueryAsNewSearch)
                .profile(profile != null
                                            ? profile.merge(uiCustomization.profile)
                                            : uiCustomization.profile)
                .listViewPagingSize(listViewPagingSize != null ? listViewPagingSize : uiCustomization.listViewPagingSize)
                .build();
        }
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {}

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class UiCustomizationBuilder {
        @SuppressWarnings("FieldMayBeFinal")
        private Map<String, String> specialUrlPrefixes = new HashMap<>();

        @SuppressWarnings("unused")
        @JsonAnySetter
        public void populateSpecialUrlPrefixes(final String contentType, final String prefix) {
            specialUrlPrefixes.put(contentType, prefix);
        }

        @JsonAnyGetter
        public Map<String, String> any() {
            return Collections.unmodifiableMap(specialUrlPrefixes);
        }
    }
}
