/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.iod.client.api.search.Entities;
import com.hp.autonomy.iod.client.api.search.FindRelatedConceptsRequestBuilder;
import com.hp.autonomy.iod.client.api.search.FindRelatedConceptsService;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RelatedConceptsServiceImpl implements RelatedConceptsService {

    @Autowired
    private FindRelatedConceptsService findRelatedConceptsService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public Entities findRelatedConcepts(final String text, final List<String> indexes, final String fieldText) throws IodErrorException {

        final Map<String, Object> params = new FindRelatedConceptsRequestBuilder()
                .setIndexes(indexes)
                .setFieldText(fieldText)
                .build();

        return findRelatedConceptsService.findRelatedConceptsWithText(apiKeyService.getApiKey(), text, params);
    }
}
