/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.searchcomponents.core.search.HavenDocument;

public class IdolDocumentsControllerTest extends AbstractDocumentsControllerTest<String, HavenDocument, AciErrorException> {
    public IdolDocumentsControllerTest() {
        super(new IdolDocumentsController(), String.class);
    }
}
