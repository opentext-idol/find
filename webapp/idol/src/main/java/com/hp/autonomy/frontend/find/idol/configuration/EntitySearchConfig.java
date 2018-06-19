/*
 * Copyright (c) 2017, Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;

@SuppressWarnings("DefaultAnnotationParam")
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = EntitySearchConfig.EntitySearchConfigBuilder.class)
public class EntitySearchConfig extends SimpleComponent<EntitySearchConfig> implements OptionalConfigurationComponent<EntitySearchConfig> {
    public static final String SECTION = "EntitySearch";

    private final ServerConfig server;
    private final Boolean enabled;
    private final Boolean absWeight;
    private final String agentBooleanField;
    private final String combine;
    private Collection<String> idolPrintFields;

    private final AnswerServerConfig answerServer;
    private final String answerServerDatabaseMatch;
    private final String answerServerContentField;
    private final Double answerServerTimeoutSecs;

    private final LinkedHashMap<String, List<String>> databaseChoices;
    private final Boolean databaseChoicesVisible;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (BooleanUtils.isTrue(enabled)) {
            if (server == null) {
                throw new ConfigException(SECTION, "Entity search is enabled but no corresponding server details have been provided");
            }
            server.basicValidate(SECTION);

            if (BooleanUtils.isTrue(answerServer.getEnabled())) {
                try {
                    answerServer.basicValidate(configSection);
                }
                catch(ConfigException e) {
                    throw new ConfigException(SECTION, e.getMessage());
                }
            }
        }
    }

    public AciServerDetails toAciServerDetails() {
        return server.toAciServerDetails();
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class EntitySearchConfigBuilder {
    }
}
