/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public class HodParametricValuesController extends ParametricValuesController<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    private final ConfigService<HodFindConfig> configService;

    @Autowired
    public HodParametricValuesController(final ParametricValuesService<HodParametricRequest, ResourceIdentifier, HodErrorException> parametricValuesService, final ConfigService<HodFindConfig> configService) {
        super(parametricValuesService);
        this.configService = configService;
    }

    @Override
    public Set<QueryTagInfo> getParametricValues(final HodParametricRequest parametricRequest) throws HodErrorException {
        final String profileName = configService.getConfig().getQueryManipulation().getProfile();
        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
        parametricRequest.setQueryProfile(new ResourceIdentifier(domain, profileName));

        return super.getParametricValues(parametricRequest);
    }
}
