/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.frontend.find.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import org.junit.Before;

public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    @Before
    public void setUp() {
        parametricValuesController = new HodParametricValuesController(parametricValuesService, new HodQueryRestrictionsBuilder());
    }
}
