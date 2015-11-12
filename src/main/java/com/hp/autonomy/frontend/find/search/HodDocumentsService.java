/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.beanconfiguration.HodCondition;
import com.hp.autonomy.frontend.find.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.web.CacheNames;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Documents;
import com.hp.autonomy.hod.client.api.textindex.query.search.Print;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryTextIndexService;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Conditional(HodCondition.class)
public class HodDocumentsService implements DocumentsService {
    @Autowired
    private ConfigService<HodFindConfig> configService;

    @Autowired
    private QueryTextIndexService<Documents> queryTextIndexService;

    @Override
    @Cacheable(CacheNames.DOCUMENTS)
    public Documents queryTextIndex(final String text, final int maxResults, final Summary summary, final List<ResourceIdentifier> indexes, final String fieldText, final Sort sort, final DateTime minDate, final DateTime maxDate) throws HodErrorException {
        return queryTextIndex(text, maxResults, summary, indexes, fieldText, sort, minDate, maxDate, false);
    }

    @Override
    @Cacheable(CacheNames.PROMOTED_DOCUMENTS)
    public Documents queryTextIndexForPromotions(final String text, final int maxResults, final Summary summary, final List<ResourceIdentifier> indexes, final String fieldText , final Sort sort, final DateTime minDate, final DateTime maxDate) throws HodErrorException {
        return queryTextIndex(text, maxResults, summary, indexes, fieldText, sort, minDate, maxDate, true);
    }

    private Documents queryTextIndex(final String text, final int maxResults, final Summary summary, final List<ResourceIdentifier> indexes, final String fieldText, final Sort sort, final DateTime minDate, final DateTime maxDate, final boolean doPromotions) throws HodErrorException {
        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
        final String profileName = configService.getConfig().getQueryManipulation().getProfile();

        final QueryRequestBuilder params = new QueryRequestBuilder()
                .setAbsoluteMaxResults(maxResults)
                .setSummary(summary)
                .setIndexes(indexes)
                .setFieldText(fieldText)
                .setQueryProfile(new ResourceIdentifier(domain, profileName))
                .setSort(sort)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
                .setPromotions(doPromotions)
                .setPrint(Print.fields)
                .setPrintFields(Arrays.asList("url", "offset", "content_type", "date")); // Need these fields for audio playback

        return queryTextIndexService.queryTextIndexWithText(text, params);
    }
}
