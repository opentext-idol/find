/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.frontend.find.core.web.CacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.hod.parametricvalues.HodParametricValuesService;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

public class CacheableParametricValuesService implements ParametricValuesService<HodParametricRequest, ResourceIdentifier, HodErrorException> {

    private final HodParametricValuesService parametricValuesService;

    public CacheableParametricValuesService(final HodParametricValuesService parametricValuesService) {
        this.parametricValuesService = parametricValuesService;
    }

    @Override
    @Cacheable(CacheNames.PARAMETRIC_VALUES)
    public Set<QueryTagInfo> getAllParametricValues(final HodParametricRequest parametricRequest) throws HodErrorException {
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }

}
