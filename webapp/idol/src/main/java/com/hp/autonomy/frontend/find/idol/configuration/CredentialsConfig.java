/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.passwords.PasswordsConfig;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jasypt.util.text.TextEncryptor;

/**
 * Configuration for username and password, where the password is stored encrypted, and is omitted
 * from API responses.
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = CredentialsConfig.CredentialsConfigBuilder.class)
public class CredentialsConfig extends SimpleComponent<CredentialsConfig>
    implements PasswordsConfig<CredentialsConfig>
{
    private final String username;
    private final String password;

    @Override
    public CredentialsConfig withoutPasswords() {
        // note: not valid for decrypting
        return toBuilder().password("").build();
    }

    @Override
    public CredentialsConfig withEncryptedPasswords(final TextEncryptor encryptor) {
        return toBuilder().password(encryptor.encrypt(password)).build();
    }

    @Override
    public CredentialsConfig withDecryptedPasswords(final TextEncryptor encryptor) {
        return toBuilder()
            // allow removing password from config by setting to empty string
            .password(encryptor.decrypt((password != null && password.isEmpty()) ? null : password))
            .build();
    }

    @Override
    public CredentialsConfig merge(final CredentialsConfig config) {
        // empty password value means 'use existing value' - see control-point-widget.js
        return super.merge(config).toBuilder()
            .password((password == null || password.isEmpty()) ? config.password : password)
            .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class CredentialsConfigBuilder {}

}
