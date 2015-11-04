/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.AbstractConfigurableAciService;
import com.hp.autonomy.frontend.configuration.ConfigService;

public class CommunityAciService extends AbstractConfigurableAciService {

    private final ConfigService<IdolFindConfig> configService;

    public CommunityAciService(final AciService aciService, final ConfigService<IdolFindConfig> configService) {
        super(aciService);

        this.configService = configService;
    }

    @Override
    public AciServerDetails getServerDetails() {
        return configService.getConfig().getCommunityDetails();
    }

}
