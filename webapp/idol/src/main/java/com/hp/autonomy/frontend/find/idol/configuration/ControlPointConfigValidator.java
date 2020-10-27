/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointApiException;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointService;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointServiceException;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControlPointConfigValidator implements Validator<ControlPointConfig> {
    private final ConfigService<IdolFindConfig> configService;
    private final HttpClient httpClient;

    @Autowired
    public ControlPointConfigValidator(
        final ConfigService<IdolFindConfig> configService,
        final HttpClient httpClient
    ) {
        this.configService = configService;
        this.httpClient = httpClient;
    }

    @Override
    public ValidationResult<String> validate(final ControlPointConfig newConfig) {
        // new config isn't valid alone because of password redaction - must merge with current
        // config to check
        final ControlPointConfig config =
            newConfig.merge(configService.getConfig().getControlPoint());
        final ControlPointService controlPointService =
            new ControlPointService(httpClient, config);

        try {
            controlPointService.checkStatus();

        } catch (final ControlPointApiException e) {
            if (e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED ||
                e.getStatusCode() == HttpStatus.SC_FORBIDDEN ||
                e.getErrorId() == ControlPointApiException.ErrorId.INVALID_GRANT
            ) {
                return new ValidationResult<>(false, "INVALID_CREDENTIALS");
            } else {
                return new ValidationResult<>(false, "UNKNOWN_ERROR");
            }

        } catch (final ControlPointServiceException e) {
            return new ValidationResult<>(false, "CONNECTION_ERROR");
        }

        return new ValidationResult<>(true);
    }

    @Override
    public Class<ControlPointConfig> getSupportedClass() {
        return ControlPointConfig.class;
    }

}
