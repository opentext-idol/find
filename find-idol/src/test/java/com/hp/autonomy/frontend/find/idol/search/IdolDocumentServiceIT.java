package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentServiceIT;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.frontend.find.idol.aci.DatabaseName;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

@TestPropertySource(properties = "hp.find.backend = IDOL")
public class IdolDocumentServiceIT extends AbstractDocumentServiceIT<DatabaseName, FindDocument, AciErrorException> {
    public IdolDocumentServiceIT() {
        super(Collections.<DatabaseName>emptyList());
    }
}
