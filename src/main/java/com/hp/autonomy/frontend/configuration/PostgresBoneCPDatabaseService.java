package com.hp.autonomy.frontend.configuration;

/*
 * $Id: $
 *
 * Copyright (c) 2013, Autonomy Systems Ltd.
 *
 * Last modified by $Author: $ on $Date: $
 */

import com.autonomy.frontend.configuration.database.BoneCPDatabaseService;
import com.autonomy.frontend.configuration.database.Postgres;

public class PostgresBoneCPDatabaseService extends BoneCPDatabaseService<Postgres, FindConfig> {

    @Override
    public Postgres getCurrentConfig() {
        final FindConfig config = getConfigService().getConfig();

        if(config != null) {
            return config.getPostgres();
        }
        else {
            return null;
        }
    }

    @Override
    public void postUpdate() {
        // actions to take after update go here
    }

}
