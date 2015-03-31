/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.frontend.find.configuration.FindConfig;
import java.util.Map;
import java.util.List;

import com.hp.autonomy.iod.client.api.textindexing.*;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexesServiceImpl implements IndexesService {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ConfigService<FindConfig> configService;

    @Autowired
    private ListIndexesService listIndexesService;

    @Override
    public Indexes listIndexes() throws IodErrorException {
        return listIndexes(apiKeyService.getApiKey());
    }

    @Override
    public com.hp.autonomy.iod.client.api.textindexing.Indexes listIndexes(final String apiKey) throws IodErrorException {
        final Map<String, Object> params = new ListIndexesRequestBuilder().build();

        return listIndexesService.listIndexes(apiKey, params);
    }

    @Override
    public List<Index> listActiveIndexes() {
        return configService.getConfig().getIod().getActiveIndexes();
    }

    @Override
    public List<Index> listVisibleIndexes() throws IodErrorException {
        final List<Index> activeIndexes = configService.getConfig().getIod().getActiveIndexes();

        if(activeIndexes.isEmpty()) {
            final Indexes indexes = listIndexes();

            final List<Index> mergedIndexes = indexes.getPublicIndexes();
            mergedIndexes.addAll(indexes.getIndexes());

            return mergedIndexes;
        }
        else {
            return activeIndexes;
        }
    }
}
