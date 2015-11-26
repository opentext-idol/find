package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsServiceIT;
import com.hp.autonomy.types.idol.QsElement;

import java.util.Collections;

public class IdolRelatedConceptsServiceIT extends AbstractRelatedConceptsServiceIT<QsElement, String, AciErrorException> {
    public IdolRelatedConceptsServiceIT() {
        super(Collections.<String>emptyList());
    }
}
