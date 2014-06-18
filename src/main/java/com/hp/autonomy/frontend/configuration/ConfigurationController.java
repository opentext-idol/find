package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.frontend.configuration.CommunityService;
import com.autonomy.frontend.configuration.ConfigException;
import com.autonomy.frontend.configuration.ConfigFileService;
import com.autonomy.frontend.configuration.ConfigResponse;
import com.autonomy.frontend.configuration.ConfigValidationException;
import com.autonomy.frontend.configuration.SecurityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * $Id: //depot/products/frontend/site-admin/trunk/webapp/src/main/java/com/autonomy/controlcentre/configuration/ConfigurationController.java#19 $
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author: matthew.gordon2 $ on $Date: 2013/11/22 $
 */

@Controller
@RequestMapping({"/useradmin/config", "/config/config"})
public class ConfigurationController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private ConfigFileService<FindConfig> configService;

	@RequestMapping(value = "/config", method = RequestMethod.GET)
    @ResponseBody
    public ConfigResponse<FindConfig> config() {
        return configService.getConfigResponse();
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    @RequestMapping(value = "/config", method = {RequestMethod.POST, RequestMethod.PUT})
	@ResponseBody
    public ResponseEntity<?> saveConfig(@RequestBody final ConfigResponse<FindConfig> configResponse) throws Exception {
        try {
            configService.updateConfig(configResponse.getConfig());
            return new ResponseEntity<>(configService.getConfigResponse(), HttpStatus.OK);
        } catch (ConfigException ce) {
            return new ResponseEntity<>(Collections.singletonMap("exception", ce.getMessage()), HttpStatus.NOT_ACCEPTABLE);
        } catch (ConfigValidationException cve) {
            return new ResponseEntity<>(Collections.singletonMap("validation", cve.getValidationErrors()), HttpStatus.NOT_ACCEPTABLE);
        }
    }


    @RequestMapping(value = "/securitytypes", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, ?> getSecurityTypes(@RequestParam final int port, @RequestParam final String host, @RequestParam final AciServerDetails.TransportProtocol protocol) {
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