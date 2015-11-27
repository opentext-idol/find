/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ServerConfig;
import com.hp.autonomy.user.UserServiceConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = IdolFindConfig.Builder.class)
public class IdolFindConfig extends AbstractConfig<IdolFindConfig> implements UserServiceConfig, AuthenticationConfig<IdolFindConfig> {

    private final CommunityAuthentication login;
    private final ServerConfig content;
    private final AciConfig aciConfig;

    @Override
    public IdolFindConfig merge(final IdolFindConfig other) {
        if (other == null) {
            return this;
        }

        return new IdolFindConfig.Builder()
            .setContent(content == null ? other.content : content.merge(other.content))
            .setLogin(login == null ? other.login : login.merge(other.login))
            .setAciConfig(aciConfig == null ? other.aciConfig : aciConfig.merge(other.aciConfig))
            .build();
    }

    @JsonIgnore
    @Override
    public AciServerDetails getCommunityDetails() {
        return login.getCommunity().toAciServerDetails();
    }

    @JsonIgnore
    @Override
    public Authentication<?> getAuthentication() {
        return login;
    }

    @Override
    public IdolFindConfig withoutDefaultLogin() {
        return new Builder(this)
            .setLogin(login.withoutDefaultLogin())
            .build();
    }

    @Override
    public IdolFindConfig generateDefaultLogin() {
        return new Builder(this)
            .setLogin(login.generateDefaultLogin())
            .build();
    }

    @Override
    public IdolFindConfig withHashedPasswords() {
        // no work to do yet
        return this;
    }

    @Override
    public void basicValidate() throws ConfigException {
        login.basicValidate();
        content.basicValidate("content");
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private CommunityAuthentication login;
        private ServerConfig content;
        private AciConfig aciConfig;

        public Builder(final IdolFindConfig config) {
            login = config.login;
            content = config.content;
            aciConfig = config.aciConfig;
        }

        public IdolFindConfig build() {
            return new IdolFindConfig(login, content, aciConfig);
        }
    }

}
