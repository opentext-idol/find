/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.frontend.find.web.CacheNames;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.parametricvalues.ParametricFieldName;
import com.hp.autonomy.parametricvalues.ParametricRequest;
import com.hp.autonomy.parametricvalues.ParametricValuesService;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

public class CacheableParametricValuesService implements ParametricValuesService {

    private final ParametricValuesService parametricValuesService;

    public CacheableParametricValuesService(final ParametricValuesService parametricValuesService) {
        this.parametricValuesService = parametricValuesService;
    }

    @Override
    @Cacheable(CacheNames.PARAMETRIC_VALUES)
    public Set<ParametricFieldName> getAllParametricValues(final ParametricRequest parametricRequest) throws HodErrorException {
        return parametricValuesService.getAllParametricValues(parametricRequest);
    }

}
