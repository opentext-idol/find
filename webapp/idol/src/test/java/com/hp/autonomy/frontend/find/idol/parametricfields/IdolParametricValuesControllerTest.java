/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mock;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolParametricValuesControllerTest extends AbstractParametricValuesControllerTest<
        IdolParametricValuesController,
        IdolParametricValuesService,
        IdolQueryRestrictions,
        IdolQueryRestrictionsBuilder,
        IdolParametricRequest,
        IdolParametricRequestBuilder,
        String,
        AciErrorException
> {
    public IdolParametricValuesControllerTest() {
        super(args -> new IdolParametricValuesController(
                args.getParametricValuesService(),
                args.getQueryRestrictionsBuilderFactory(),
                args.getParametricRequestBuilderFactory()
        ), () -> mock(IdolParametricValuesService.class));
    }
}
