/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.beanconfiguration.HavenSearchHodConfiguration;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mock;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchHodConfiguration.class, properties = {"mock.authentication=false", "mock.authenticationRetriever=false"}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<
        HodParametricValuesController,
        HodParametricValuesService,
        HodQueryRestrictions,
        HodQueryRestrictionsBuilder,
        HodParametricRequest,
        HodParametricRequestBuilder,
        ResourceName,
        HodErrorException
> {
    public HodParametricValuesControllerTest() {
        super(args -> new HodParametricValuesController(
                args.getParametricValuesService(),
                args.getQueryRestrictionsBuilderFactory(),
                args.getParametricRequestBuilderFactory()
        ), () -> mock(HodParametricValuesService.class));
    }
}
