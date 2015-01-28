/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.frontend.find.configuration.FindConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IndexesServiceImpl implements IndexesService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ConfigService<FindConfig> configService;

    @Override
    public Indexes listIndexes() {
        return listIndexes(apiKeyService.getApiKey());
    }

    @Override
    public Indexes listIndexes(final String apiKey) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("apikey", apiKey);

        return restTemplate.getForObject("https://api.idolondemand.com/1/api/sync/listindexes/v1?apikey={apikey}", Indexes.class, parameters);
    }

    @Override
    public List<Index> listActiveIndexes() {
        return configService.getConfig().getIod().getActiveIndexes();
    }

    @Override
    public List<Index> listVisibleIndexes() {
        final List<Index> activeIndexes = configService.getConfig().getIod().getActiveIndexes();

        if(activeIndexes.isEmpty()) {
            final Indexes indexes = listIndexes();
            final List<Index> mergedIndexes = indexes.getPublicIndexes();

            for(final PrivateIndex privateIndex : indexes.getPrivateIndexes()) {
                mergedIndexes.add(privateIndex.toIndex());
            }

            return mergedIndexes;
        }
        else {
            return activeIndexes;
        }
    }
}
