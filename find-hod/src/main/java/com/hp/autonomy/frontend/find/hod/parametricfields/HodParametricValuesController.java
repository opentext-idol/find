/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValuesController;
import com.hp.autonomy.frontend.find.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.frontend.find.core.web.AuthenticationInformationRetriever;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationCapable;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

@Controller
@RequestMapping(ParametricValuesController.PARAMETRIC_VALUES_PATH)
public class HodParametricValuesController extends ParametricValuesController<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    private final ConfigService<? extends QueryManipulationCapable> configService;
    private final AuthenticationInformationRetriever<HodAuthentication> authenticationInformationRetriever;

    @Autowired
    public HodParametricValuesController(final ParametricValuesService<HodParametricRequest, ResourceIdentifier, HodErrorException> parametricValuesService, final ConfigService<? extends QueryManipulationCapable> configService, final QueryRestrictionsBuilder<ResourceIdentifier> queryRestrictionsBuilder, final AuthenticationInformationRetriever<HodAuthentication> authenticationInformationRetriever) {
        super(parametricValuesService, queryRestrictionsBuilder);
        this.configService = configService;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
    }

    @Override
    protected HodParametricRequest buildParametricRequest(final Set<String> fieldNames, final QueryRestrictions<ResourceIdentifier> queryRestrictions) {
        final String profileName = configService.getConfig().getQueryManipulation().getProfile();
        final String domain = authenticationInformationRetriever.getAuthentication().getPrincipal().getApplication().getDomain();

        return new HodParametricRequest.Builder()
                .setFieldNames(fieldNames)
                .setQueryRestrictions(queryRestrictions)
                .setQueryProfile(new ResourceIdentifier(domain, profileName))
                .build();
    }
}
