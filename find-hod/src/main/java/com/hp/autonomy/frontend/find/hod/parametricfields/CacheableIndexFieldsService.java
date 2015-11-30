/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.web.CacheNames;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.fields.IndexFieldsService;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

public class CacheableIndexFieldsService implements IndexFieldsService {

    private final IndexFieldsService indexFieldsService;

    public CacheableIndexFieldsService(final IndexFieldsService indexFieldsService) {
        this.indexFieldsService = indexFieldsService;
    }

    @Override
    @Cacheable(CacheNames.PARAMETRIC_FIELDS)
    public Set<String> getParametricFields(final ResourceIdentifier index) throws HodErrorException {
        return indexFieldsService.getParametricFields(index);
    }

    @Override
    @Cacheable(CacheNames.PARAMETRIC_FIELDS)
    public Set<String> getParametricFields(final TokenProxy<?, TokenType.Simple> tokenProxy, final ResourceIdentifier resourceIdentifier) throws HodErrorException {
        return indexFieldsService.getParametricFields(tokenProxy, resourceIdentifier);
    }
}
