/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;

public interface DocumentsService {

    Documents<FindDocument> queryTextIndex(
            String text,
            int maxResults,
            Summary summary,
            List<ResourceIdentifier> indexes,
            String fieldText,
            Sort sort,
            DateTime minDate,
            DateTime maxDate
    ) throws HodErrorException;

    Documents<FindDocument> queryTextIndexForPromotions(
            String text,
            int maxResults,
            Summary summary,
            List<ResourceIdentifier> indexes,
            String fieldText,
            Sort sort,
            DateTime minDate,
            DateTime maxDate
    ) throws HodErrorException;

    List<FindDocument> findSimilar(
            Set<ResourceIdentifier> indexes,
            String reference
    ) throws HodErrorException;

}
