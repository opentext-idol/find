/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.ConfigResponse;
import com.hp.autonomy.frontend.configuration.validation.ConfigValidationException;
import com.hp.autonomy.frontend.logging.Markers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;

@Controller
@RequestMapping({"/api/admin/config", "/api/config/config"})
@Slf4j
// TODO have a think about this (maybe make it a generic type and instantiate in config class)
public class HodConfigurationController {

    @Autowired
    private ConfigFileService<HodFindConfig> configService;

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ResponseBody
    public ConfigResponse<HodFindConfig> config() {
        return configService.getConfigResponse();
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @RequestMapping(value = "/config", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    public ResponseEntity<?> saveConfig(@RequestBody final ConfigResponse<HodFindConfig> configResponse) throws Exception {
        try {
            log.info(Markers.AUDIT, "REQUESTED CHANGE APPLICATION CONFIGURATION");
            configService.updateConfig(configResponse.getConfig());
            log.info(Markers.AUDIT, "CHANGED APPLICATION CONFIGURATION");
            return new ResponseEntity<>(configService.getConfigResponse(), HttpStatus.OK);
        } catch (final ConfigException ce) {
            log.info(Markers.AUDIT, "CHANGE APPLICATION CONFIGURATION FAILED");
            return new ResponseEntity<>(Collections.singletonMap("exception", ce.getMessage()), HttpStatus.NOT_ACCEPTABLE);
        } catch (final ConfigValidationException cve) {
            log.info(Markers.AUDIT, "CHANGE APPLICATION CONFIGURATION FAILED");
            return new ResponseEntity<>(Collections.singletonMap("validation", cve.getValidationErrors()), HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
