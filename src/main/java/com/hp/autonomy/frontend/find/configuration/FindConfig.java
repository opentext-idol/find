/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.Authentication;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.PasswordsConfig;
import com.hp.autonomy.hod.sso.HodSsoConfig;
import com.hp.autonomy.frontend.configuration.RedisConfig;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasypt.util.text.TextEncryptor;

import java.util.Set;

@JsonDeserialize(builder = FindConfig.Builder.class)
@Getter
@EqualsAndHashCode(callSuper = false)
public class FindConfig extends AbstractConfig<FindConfig> implements AuthenticationConfig<FindConfig>, PasswordsConfig<FindConfig>, HodSsoConfig {

    private final Authentication<?> login;
    private final IodConfig iod;
    private final Set<String> allowedOrigins;
    private final RedisConfig redis;

    private FindConfig(final Builder builder) {
        this.login = builder.login;
        this.iod = builder.iod;
        this.allowedOrigins = builder.allowedOrigins;
        this.redis = builder.redis;
    }

    @Override
    public FindConfig merge(final FindConfig config) {
        if(config != null) {
            final Builder builder = new Builder();

            builder.setLogin(this.login == null ? config.login : this.login.merge(config.login));
            builder.setIod(this.iod == null ? config.iod : this.iod.merge(config.iod));
            builder.setAllowedOrigins(this.allowedOrigins == null ? config.allowedOrigins : this.allowedOrigins);
            builder.setRedis(this.redis == null ? config.redis : this.redis.merge(config.redis));

            return builder.build();
        }
        else {
            return this;
        }
    }

    @Override
    public FindConfig withoutDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withoutDefaultLogin();

        return builder.build();
    }

    @Override
    public FindConfig generateDefaultLogin() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.generateDefaultLogin();

        return builder.build();
    }

    @Override
    public FindConfig withHashedPasswords() {
        final Builder builder = new Builder(this);

        builder.login = builder.login.withHashedPasswords();

        return builder.build();
    }

    @Override
    public void basicValidate() throws ConfigException {
        if(!this.login.getMethod().equalsIgnoreCase("default")){
            this.login.basicValidate();
        }
    }

    @Override
    public FindConfig withoutPasswords() {
        final Builder builder = new Builder(this);

        builder.login = login.withoutPasswords();

        return builder.build();
    }

    @Override
    public FindConfig withEncryptedPasswords(final TextEncryptor encryptor) {
        return this;
    }

    @Override
    public FindConfig withDecryptedPasswords(final TextEncryptor encryptor) {
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
        return getIod().getApiKey();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @Setter
    @Accessors(chain = true)
    public static class Builder {

        private Authentication<?> login;
        private IodConfig iod;
        private Set<String> allowedOrigins;
        private RedisConfig redis;

        public Builder() {}

        public Builder(final FindConfig config) {
            this.login = config.login;
            this.iod = config.iod;
            this.allowedOrigins = config.allowedOrigins;
            this.redis = config.redis;
        }

        public FindConfig build() {
            return new FindConfig(this);
        }
    }
}
