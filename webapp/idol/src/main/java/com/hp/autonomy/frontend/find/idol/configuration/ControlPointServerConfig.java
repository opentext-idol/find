/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.passwords.PasswordsConfig;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointServerDetails;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.jasypt.util.text.TextEncryptor;

import java.util.Arrays;

/**
 * Configuration for the ControlPoint server used to apply policies to documents.
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = ControlPointServerConfig.ControlPointServerConfigBuilder.class)
public class ControlPointServerConfig extends SimpleComponent<ControlPointServerConfig>
    implements PasswordsConfig<ControlPointServerConfig>
{
    private final String protocol;
    private final String host;
    private final Integer port;
    private final CredentialsConfig credentials;
    private final String basePath;

    private void failValidate(final String component, final String message) throws ConfigException {
        throw new ConfigException(component, component + ": " + message);
    }

    @Override
    public void basicValidate(final String component) throws ConfigException {
        if (protocol == null) failValidate(component, "protocol must be specified");
        if (!Arrays.asList("http", "https").contains(protocol.toLowerCase())) {
            failValidate(component, "protocol must be HTTP or HTTPS");
        }
        if (host == null) failValidate(component, "host must be specified");
        if (StringUtils.isBlank(host)) failValidate(component, "host must not be blank");
        if (port == null) failValidate(component, "port must be specified");
        if (port <= 0 || port > 65535) failValidate(component, "port must be between 1 and 65535");
        if (credentials != null) credentials.basicValidate(component);
    }

    @Override
    public ControlPointServerConfig withoutPasswords() {
        return toBuilder()
            .credentials(credentials == null ? null : credentials.withoutPasswords())
            .build();
    }

    @Override
    public ControlPointServerConfig withEncryptedPasswords(final TextEncryptor encryptor) {
        return toBuilder()
            .credentials(credentials == null ? null : credentials.withEncryptedPasswords(encryptor))
            .build();
    }

    @Override
    public ControlPointServerConfig withDecryptedPasswords(final TextEncryptor encryptor) {
        return toBuilder()
            .credentials(credentials == null ? null : credentials.withDecryptedPasswords(encryptor))
            .build();
    }

    public ControlPointServerDetails toServerDetails() {
        final ControlPointServerDetails.ControlPointServerDetailsBuilder builder =
            ControlPointServerDetails.builder()
                .protocol(protocol.toLowerCase())
                .host(host)
                .port(port)
                .username(credentials == null ? null : credentials.getUsername())
                .password(credentials == null ? null : credentials.getPassword());
        // fields with defaults we shouldn't override with null
        if (basePath != null) builder.basePath(basePath);
        return builder.build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ControlPointServerConfigBuilder {}

}
