/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.junit.Before;

public class IdolParametricValuesControllerTest extends AbstractParametricValuesControllerTest<IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> {
    @Before
    public void setUp() {
        parametricValuesController = new IdolParametricValuesController(parametricValuesService, new IdolQueryRestrictions.Builder(), new IdolParametricRequest.Builder());
    }
}
