/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.PasswordsConfig;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import com.hp.autonomy.frontend.find.core.configuration.MapConfig;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.hod.sso.HodSsoConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.hod.configuration.HodSearchCapable;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasypt.util.text.TextEncryptor;

import java.util.Set;

@JsonDeserialize(builder = HodFindConfig.Builder.class)
@Getter
@EqualsAndHashCode(callSuper = false)
public class HodFindConfig extends AbstractConfig<HodFindConfig> implements AuthenticationConfig<HodFindConfig>, HodSearchCapable, PasswordsConfig<HodFindConfig>, HodSsoConfig, MapConfig {
    private final Authentication<?> login;
    private final HsodConfig hsod;
    private final IodConfig iod;
    private final QueryManipulationConfig queryManipulation;
    private final Set<String> allowedOrigins;
    private final RedisConfig redis;
    private final FieldsInfo fieldsInfo;
    private final MapConfiguration map;

    private HodFindConfig(final Builder builder) {
        login = builder.login;
        hsod = builder.hsod;
        iod = builder.iod;
        allowedOrigins = builder.allowedOrigins;
        redis = builder.redis;
        fieldsInfo = builder.fieldsInfo;
        queryManipulation = builder.queryManipulation;
        map = builder.map;
    }

    @Override
    public HodFindConfig merge(final HodFindConfig config) {
        if (config != null) {
            return new Builder()
                    .setLogin(login == null ? config.login : login.merge(config.login))
                    .setIod(iod == null ? config.iod : iod.merge(config.iod))
                    .setAllowedOrigins(allowedOrigins == null ? config.allowedOrigins : allowedOrigins)
                    .setRedis(redis == null ? config.redis : redis.merge(config.redis))
                    .setQueryManipulation(queryManipulation == null ? config.queryManipulation : queryManipulation.merge(config.queryManipulation))
                    .setHsod(hsod == null ? config.hsod : hsod.merge(config.hsod))
                    .setFieldsInfo(fieldsInfo == null ? config.fieldsInfo : fieldsInfo.merge(config.fieldsInfo))
                    .setMap(map == null ? config.map : map.merge(config.map))
                    .build();
        } else {
            return this;
        }
    }

    @Override
    public HodFindConfig withoutDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withoutDefaultLogin();

        return builder.build();
    }

    @Override
    public HodFindConfig generateDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.generateDefaultLogin();

        return builder.build();
    }

    @Override
    public HodFindConfig withHashedPasswords() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withHashedPasswords();

        return builder.build();
    }

    @Override
    public void basicValidate() throws ConfigException {
        redis.basicValidate();
        queryManipulation.basicValidate();

        if (map != null) {
            map.basicValidate("map");
        }

        if (!"default".equalsIgnoreCase(login.getMethod())) {
            login.basicValidate();
        }
    }

    @Override
    public HodFindConfig withoutPasswords() {
        final Builder builder = new Builder(this);

        builder.login = login.withoutPasswords();

        return builder.build();
    }

    @Override
    public HodFindConfig withEncryptedPasswords(final TextEncryptor encryptor) {
        return this;
    }

    @Override
    public HodFindConfig withDecryptedPasswords(final TextEncryptor encryptor) {
        return this;
    }

    @Override
    @JsonIgnore
    public Authentication<?> getAuthentication() {
        return login;
    }

    @Override
    @JsonIgnore
    public String getApiKey() {
        return iod.getApiKey();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @NoArgsConstructor
    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Authentication<?> login;
        private HsodConfig hsod;
        private IodConfig iod;
        private Set<String> allowedOrigins;
        private RedisConfig redis;
        private QueryManipulationConfig queryManipulation;
        private FieldsInfo fieldsInfo;
        private MapConfiguration map;

        public Builder(final HodFindConfig config) {
            login = config.login;
            hsod = config.hsod;
            iod = config.iod;
            allowedOrigins = config.allowedOrigins;
            redis = config.redis;
            queryManipulation = config.queryManipulation;
            fieldsInfo = config.fieldsInfo;
            map = config.map;
        }

        public HodFindConfig build() {
            return new HodFindConfig(this);
        }
    }
}
