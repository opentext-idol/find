/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.frontend.find.core.search.FindDocument;

public class IdolDocumentsControllerTest extends AbstractDocumentsControllerTest<String, FindDocument, AciErrorException> {
    public IdolDocumentsControllerTest() {
        super(new IdolDocumentsController(), String.class);
    }
}
