/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesServiceIT;
import com.hp.autonomy.idol.parametricvalues.IdolParametricRequest;

import java.util.Collections;

public class IdolParametricValuesServiceIT extends AbstractParametricValuesServiceIT<IdolParametricRequest, String, AciErrorException> {
    public IdolParametricValuesServiceIT() {
        super(Collections.<String>emptyList(), Collections.singleton("CATEGORY"));
    }
}
