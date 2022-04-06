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

package com.hp.autonomy.frontend.find.idol.applications;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("WeakerAccess")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = CustomApplication.CustomApplicationBuilder.class)
public class CustomApplication extends SimpleComponent<CustomApplication> implements OptionalConfigurationComponent<CustomApplication> {
    private final String applicationName;
    private final String url;
    private final String icon;
    private final Boolean enabled;
    private final boolean openInNewTab;

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class CustomApplicationBuilder {
        private String icon = "";
        private Boolean enabled = true;
    }
}
