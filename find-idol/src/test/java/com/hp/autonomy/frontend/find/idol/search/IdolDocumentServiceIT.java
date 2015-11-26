package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentServiceIT;
import com.hp.autonomy.frontend.find.core.search.FindDocument;

import java.util.Collections;

public class IdolDocumentServiceIT extends AbstractDocumentServiceIT<String, FindDocument, AciErrorException> {
    public IdolDocumentServiceIT() {
        super(Collections.<String>emptyList());
    }
}
