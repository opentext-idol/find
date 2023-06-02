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

package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.passwords.PasswordsConfig;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.TextEncryptor;

/**
 * Top-level configuration section - {@link ControlPointServerConfig} which can be disabled.
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@JsonDeserialize(builder = ControlPointConfig.ControlPointConfigBuilder.class)
public class ControlPointConfig extends SimpleComponent<ControlPointConfig>
    implements OptionalConfigurationComponent<ControlPointConfig>,
    PasswordsConfig<ControlPointConfig>
{
    static final String SECTION = "ControlPoint";

    private final Boolean enabled;
    private final ControlPointServerConfig server;

    @Override
    public void basicValidate(final String configSection) throws ConfigException {
        if (BooleanUtils.isTrue(enabled)) {
            if (server == null) {
                throw new ConfigException(SECTION,
                    "ControlPoint is enabled but server details are missing");
            }
            server.basicValidate(SECTION);
        }
    }

    @Override
    public ControlPointConfig withoutPasswords() {
        return toBuilder().server(server == null ? null : server.withoutPasswords()).build();
    }

    @Override
    public ControlPointConfig withEncryptedPasswords(final TextEncryptor encryptor) {
        return toBuilder()
            .server(server == null ? null : server.withEncryptedPasswords(encryptor))
            .build();
    }

    @Override
    public ControlPointConfig withDecryptedPasswords(final TextEncryptor encryptor) {
        return toBuilder()
            .server(server == null ? null : server.withDecryptedPasswords(encryptor))
            .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ControlPointConfigBuilder {}

}
