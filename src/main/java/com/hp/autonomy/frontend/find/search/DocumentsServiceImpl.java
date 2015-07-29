/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.find.QueryProfileService;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DocumentsServiceImpl implements DocumentsService {

    @Autowired
    private QueryProfileService queryProfileService;

    @Autowired
    private QueryTextIndexService<Documents> queryTextIndexService;

    @Override
    public Documents queryTextIndex(final String text, final int maxResults, final Summary summary, final List<ResourceIdentifier> indexes, final String fieldText, final Sort sort, final DateTime minDate, final DateTime maxDate) throws HodErrorException {
        return queryTextIndex(text, maxResults, summary, indexes, fieldText, sort, minDate, maxDate, false);
    }

    @Override
    public Documents queryTextIndexForPromotions(final String text, final int maxResults, final Summary summary, final List<ResourceIdentifier> indexes, final String fieldText , final Sort sort, final DateTime minDate, final DateTime maxDate) throws HodErrorException {
        return queryTextIndex(text, maxResults, summary, indexes, fieldText, sort, minDate, maxDate, true);
    }

    private Documents queryTextIndex(final String text, final int maxResults, final Summary summary, final List<ResourceIdentifier> indexes, final String fieldText, final Sort sort, final DateTime minDate, final DateTime maxDate, final boolean doPromotions) throws HodErrorException {
        final QueryRequestBuilder params = new QueryRequestBuilder()
                .setAbsoluteMaxResults(maxResults)
                .setSummary(summary)
                .setIndexes(indexes)
                .setFieldText(fieldText)
                .setQueryProfile(queryProfileService.getQueryProfile())
                .setSort(sort)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
                .setPromotions(doPromotions)
                .setPrint(Print.fields)
                .setPrintFields(Arrays.asList("url", "offset", "content_type", "date")); // Need these fields for audio playback

        return queryTextIndexService.queryTextIndexWithText(text, params);
    }
}
