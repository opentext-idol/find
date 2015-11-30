/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.AbstractDocumentServiceIT;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;

import java.util.Arrays;

public class HodDocumentServiceIT extends AbstractDocumentServiceIT<ResourceIdentifier, HodFindDocument, HodErrorException> {
    public HodDocumentServiceIT() {
        super(Arrays.asList(ResourceIdentifier.WIKI_ENG, ResourceIdentifier.NEWS_ENG));
    }
}
