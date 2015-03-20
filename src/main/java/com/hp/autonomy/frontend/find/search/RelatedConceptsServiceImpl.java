/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.damnhandy.uri.template.UriTemplate;
import com.hp.autonomy.frontend.find.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatedConceptsServiceImpl implements RelatedConceptsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public List<Entity> findRelatedConcepts(final String text, final List<String> indexes) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("text", text);
        parameters.put("indexes", indexes);
        parameters.put("apikey", apiKeyService.getApiKey());

        final String url = UriTemplate.fromTemplate("https://api.idolondemand.com/1/api/sync/findrelatedconcepts/v1{?apikey}{&text}{&indexes*}").expand(parameters);
        return restTemplate.getForObject(URI.create(url), Entities.class).getEntities();
    }
}
