/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.web.FindCacheNames;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.searchcomponents.hod.fields.IndexFieldsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("indexFieldsService")
public class CacheableIndexFieldsService extends IndexFieldsServiceImpl {
    @Autowired
    public CacheableIndexFieldsService(final RetrieveIndexFieldsService retrieveIndexFieldsService) {
        super(retrieveIndexFieldsService);
    }

    @Override
    @Cacheable(FindCacheNames.PARAMETRIC_FIELDS)
    public Set<String> getParametricFields(final ResourceIdentifier index) throws HodErrorException {
        return super.getParametricFields(index);
    }

    @Override
    @Cacheable(FindCacheNames.PARAMETRIC_FIELDS)
    public Set<String> getParametricFields(final TokenProxy<?, TokenType.Simple> tokenProxy, final ResourceIdentifier resourceIdentifier) throws HodErrorException {
        return super.getParametricFields(tokenProxy, resourceIdentifier);
    }
}
