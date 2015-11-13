/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.similar;

import com.hp.autonomy.frontend.find.search.FindDocument;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;

import java.util.List;
import java.util.Set;

public interface SimilarDocumentsService {

    /**
     * Find documents from the given indexes which are similar to documents from the indexes with the given reference.
     * The reference must be present in at least one of the indexes.
     * @param indexes The domain and names of the target indexes
     * @param reference The reference string
     * @return List of similar documents
     * @throws HodErrorException
     */
    List<FindDocument> findSimilar(Set<ResourceIdentifier> indexes, String reference) throws HodErrorException;

}
