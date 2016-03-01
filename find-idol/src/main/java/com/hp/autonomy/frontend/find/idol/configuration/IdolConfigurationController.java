/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.CommunityService;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigValidationException;
import com.hp.autonomy.frontend.configuration.SecurityType;
import com.hp.autonomy.frontend.logging.Markers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping({"/api/useradmin/config", "/api/config/config"})
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
        final List<String> typeNames = new ArrayList<>();

        for (final SecurityType type : types) {
            typeNames.add(type.getName());
        }

        return Collections.singletonMap("securityTypes", typeNames);
    }

}