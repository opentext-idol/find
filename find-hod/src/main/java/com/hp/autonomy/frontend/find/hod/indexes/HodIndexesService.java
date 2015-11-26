/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.frontend.find.core.indexes.IndexesService;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.databases.Database;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HodIndexesService extends IndexesService<Database, HodErrorException> {
    Resources listIndexes(final TokenProxy<?, TokenType.Simple> tokenProxy) throws HodErrorException;

    List<ResourceIdentifier> listActiveIndexes();
}
