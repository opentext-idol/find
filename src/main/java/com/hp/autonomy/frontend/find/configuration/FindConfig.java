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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasypt.util.text.TextEncryptor;

@JsonDeserialize(builder = FindConfig.Builder.class)
@Getter
@EqualsAndHashCode(callSuper = false)
public class FindConfig extends AbstractConfig<FindConfig> implements AuthenticationConfig<FindConfig>, PasswordsConfig<FindConfig> {

    private final Authentication<?> login;
    private final IodConfig iod;

    private FindConfig(final Builder builder) {
        this.login = builder.login;
        this.iod = builder.iod;
    }

    @Override
    public FindConfig merge(final FindConfig config) {
        if(config != null) {
            final Builder builder = new Builder();

            builder.setLogin(this.login == null ? config.login : this.login.merge(config.login));
            builder.setIod(this.iod == null ? config.iod : this.iod.merge(config.iod));

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

    @JsonPOJOBuilder(withPrefix = "set")
    @Setter
    @Accessors(chain = true)
    public static class Builder {

        private Authentication<?> login;
        private IodConfig iod;

        public Builder() {}

        public Builder(final FindConfig config) {
            this.login = config.login;
            this.iod = config.iod;
        }

        public FindConfig build() {
            return new FindConfig(this);
        }
    }

}
