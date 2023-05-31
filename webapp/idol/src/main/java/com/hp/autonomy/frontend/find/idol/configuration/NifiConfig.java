/*
 * Copyright 2021 Open Text.
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

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Top-level configuration section for NiFi.
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = NifiConfig.NifiConfigBuilder.class)
public class NifiConfig extends SimpleComponent<NifiConfig>
    implements OptionalConfigurationComponent<NifiConfig>
{
    static final String SECTION = "Nifi";

    private final Boolean enabled;
    private final ServerConfig server;
    /**
     * The name of the NiFi action which responds with a list of actions.
     */
    @Builder.Default
    private final String listAction = "listactions";

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (BooleanUtils.isTrue(enabled)) {
            if (server == null) {
                throw new ConfigException(SECTION,
                    "NiFi is enabled but server details are missing");
            }
            server.basicValidate(SECTION);
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class NifiConfigBuilder {}

}
