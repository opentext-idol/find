/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import java.util.List;
import com.hp.autonomy.iod.client.api.search.Entities;
import com.hp.autonomy.iod.client.error.IodErrorException;

public interface RelatedConceptsService {

    public Entities findRelatedConcepts(String text, List<String> indexes) throws IodErrorException;
}
