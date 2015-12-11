/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.HostedFindApplication;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesServiceIT;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.parametricvalues.HodParametricRequest;
import org.springframework.boot.test.SpringApplicationConfiguration;

import java.util.Arrays;
import java.util.Collections;

@SpringApplicationConfiguration(classes = HostedFindApplication.class)
public class HodParametricValuesServiceIT extends AbstractParametricValuesServiceIT<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    public HodParametricValuesServiceIT() {
        super(Arrays.asList(ResourceIdentifier.WIKI_ENG, ResourceIdentifier.NEWS_ENG), Collections.singleton("Some field")); //TODO get real field name
    }
}
