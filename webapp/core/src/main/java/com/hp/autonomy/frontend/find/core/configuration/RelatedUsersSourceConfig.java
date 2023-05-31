/*
 * Copyright 2020 Open Text.
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

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@JsonDeserialize(builder = RelatedUsersSourceConfig.RelatedUsersSourceConfigBuilder.class)
public class RelatedUsersSourceConfig extends SimpleComponent<RelatedUsersSourceConfig> {
    private final String agentStoreProfilesDatabase;
    private final String namedArea;
    private final List<UserDetailsFieldConfig> userDetailsFields;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (agentStoreProfilesDatabase == null) {
            throw new ConfigException(configSection, "agentStoreProfilesDatabase must be provided");
        }
        if (namedArea == null) {
            throw new ConfigException(configSection, "namedArea must be provided");
        }

        if (userDetailsFields == null) {
            throw new ConfigException(configSection, "userDetailsField must be provided");
        }
        for (int i = 0; i < userDetailsFields.size(); i++) {
            final UserDetailsFieldConfig field = userDetailsFields.get(i);
            if (field == null) {
                throw new ConfigException(
                    configSection + ".userDetailsField", "item at index " + i + " is null");
            }
            field.basicValidate(configSection + ".userDetailsField." + i);
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class RelatedUsersSourceConfigBuilder {
        private String agentStoreProfilesDatabase = "profile";
        private String namedArea = "default";
        private List<UserDetailsFieldConfig> userDetailsFields = Collections.emptyList();
    }

}
