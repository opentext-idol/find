package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsServiceIT;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import com.hp.autonomy.types.idol.QsElement;

import java.util.Collections;

public class IdolRelatedConceptsServiceIT extends AbstractRelatedConceptsServiceIT<QsElement, DatabaseName, AciErrorException> {
    public IdolRelatedConceptsServiceIT() {
        super(Collections.<DatabaseName>emptyList());
    }
}
