/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsServiceIT;
import com.hp.autonomy.types.idol.QsElement;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

@TestPropertySource(properties = "hp.find.backend = IDOL")
public class IdolRelatedConceptsServiceIT extends AbstractRelatedConceptsServiceIT<QsElement, String, AciErrorException> {
    public IdolRelatedConceptsServiceIT() {
        super(Collections.<String>emptyList());
    }
}
