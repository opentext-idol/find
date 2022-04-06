/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import com.hp.autonomy.frontend.configuration.aci.CommunityService;
import com.hp.autonomy.frontend.configuration.validation.ConfigValidationException;
import com.hp.autonomy.frontend.configuration.validation.ValidationResult;
import com.hp.autonomy.frontend.configuration.validation.ValidationResults;
import com.hp.autonomy.frontend.logging.Markers;
import com.hp.autonomy.types.idol.responses.SecurityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping({"/api/admin/config", "/api/config/config"})
public class IdolConfigurationController {
    private final CommunityService communityService;
    private final ConfigFileService<IdolFindConfig> configService;

    @Autowired
    public IdolConfigurationController(final CommunityService communityService, final ConfigFileService<IdolFindConfig> configService) {
        this.communityService = communityService;
        this.configService = configService;
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public Object config() {
        return configService.getConfigResponse();
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @RequestMapping(value = "/config", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> saveConfig(@RequestBody final IdolFindConfigWrapper configResponse) throws Exception {
        log.info(Markers.AUDIT, "REQUESTED UPDATE CONFIGURATION");

        //TODO: move this to CommunityAuthentication validator once FIND-1254 is complete
        if (LoginTypes.DEFAULT.equals(configResponse.getConfig().getAuthentication().getMethod())) {
            final ValidationResults validationResults = new ValidationResults.Builder()
                    .put("login", new ValidationResult<>(false, CommunityAuthenticationValidation.DEFAULT_LOGIN))
                    .build();
            return new ResponseEntity<>(Collections.singletonMap("validation", validationResults), HttpStatus.NOT_ACCEPTABLE);
        }

        try {
            configService.updateConfig(configResponse.getConfig());
            final Object response = configService.getConfigResponse();
            log.info(Markers.AUDIT, "UPDATED CONFIGURATION");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (final ConfigException ce) {
            return new ResponseEntity<>(Collections.singletonMap("exception", ce.getMessage()), HttpStatus.NOT_ACCEPTABLE);
        } catch (final ConfigValidationException cve) {
            return new ResponseEntity<>(Collections.singletonMap("validation", cve.getValidationErrors()), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @RequestMapping(value = "/securitytypes", method = RequestMethod.GET)
    public Map<String, List<String>> getSecurityTypes(@RequestParam final int port, @RequestParam final String host, @RequestParam final AciServerDetails.TransportProtocol protocol) {
        if (port <= 0 || host == null || protocol == null) {
            throw new IllegalArgumentException("Host, port and protocol must be supplied.");
        }

        final List<SecurityType> types = communityService.getSecurityTypes(new AciServerDetails(protocol, host, port));
        final List<String> typeNames = types.stream().map(SecurityType::getName).collect(Collectors.toList());

        return Collections.singletonMap("securityTypes", typeNames);
    }

    private enum CommunityAuthenticationValidation {
        DEFAULT_LOGIN
    }
}
