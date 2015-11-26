package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsServiceIT;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import com.hp.autonomy.types.idol.QsElement;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

@TestPropertySource(properties = "hp.find.backend = IDOL")
public class IdolRelatedConceptsServiceIT extends AbstractRelatedConceptsServiceIT<QsElement, DatabaseName, AciErrorException> {
    public IdolRelatedConceptsServiceIT() {
        super(Collections.<DatabaseName>emptyList());
    }
}
