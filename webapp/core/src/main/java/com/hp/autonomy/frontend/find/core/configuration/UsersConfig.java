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
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;

@Getter
@Builder
@JsonDeserialize(builder = UsersConfig.UsersConfigBuilder.class)
public class UsersConfig extends SimpleComponent<UsersConfig> {
    static final String SECTION = "users";

    private final RelatedUsersConfig relatedUsers;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (relatedUsers == null) {
            throw new ConfigException(configSection, "relatedUsers must be provided");
        }
        relatedUsers.basicValidate(configSection + ".relatedUsers");
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class UsersConfigBuilder {
        private RelatedUsersConfig relatedUsers = RelatedUsersConfig.builder().build();
    }

}
