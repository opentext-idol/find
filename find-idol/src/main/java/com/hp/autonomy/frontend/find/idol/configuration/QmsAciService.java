/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.AbstractConfigurableAciService;
import com.hp.autonomy.frontend.configuration.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QmsAciService extends AbstractConfigurableAciService implements OptionalAciService {

    private final ConfigService<IdolFindConfig> configService;

    @Autowired
    public QmsAciService(@Qualifier("aciService") final AciService aciService, final ConfigService<IdolFindConfig> configService) {
        super(aciService);

        this.configService = configService;
    }

    @Override
    public AciServerDetails getServerDetails() {
        final QueryManipulation queryManipulation = configService.getConfig().getQueryManipulation();
        return queryManipulation != null && queryManipulation.isEnabled() ? queryManipulation.getServer().toAciServerDetails() : null;
    }

    @Override
    public boolean isEnabled() {
        final QueryManipulation queryManipulation = configService.getConfig().getQueryManipulation();
        return queryManipulation != null && queryManipulation.isEnabled();
    }

}
