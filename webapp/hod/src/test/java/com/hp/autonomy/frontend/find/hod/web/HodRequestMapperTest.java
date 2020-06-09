/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.core.web.RequestMapperTest;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.api.textindex.query.search.Sort;
import com.hp.autonomy.hod.client.api.textindex.query.search.Summary;
import com.hp.autonomy.searchcomponents.hod.requests.HodRequestBuilderConfiguration;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;


@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HodRequestBuilderConfiguration.class)
public class HodRequestMapperTest extends RequestMapperTest<HodQueryRequest, HodQueryRestrictions, ResourceName> {
    @ClassRule
    public static final SpringClassRule SCR = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;
    @Autowired
    private HodQueryRequestBuilder queryRequestBuilder;

    @Override
    protected RequestMapper<HodQueryRequest> constructRequestMapper() {
        return new HodRequestMapper(queryRestrictionsBuilder, queryRequestBuilder);
    }

    @Override
    protected String completeJsonObject() throws IOException {
        return IOUtils.toString(HodRequestMapperTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/hod/web/search-request.json"));
    }

    @Override
    protected String minimalJsonObject() throws IOException {
        return IOUtils.toString(HodRequestMapperTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/hod/web/search-request-minimal.json"));
    }

    @Override
    protected void validateDatabases(final List<ResourceName> databases) {
        assertThat(databases, hasItem(is(new ResourceName("ClassicalDomain", "ClassicalLiterature"))));
        assertThat(databases, hasItem(is(new ResourceName("ClassicalDomain", "EpicLiterature"))));
    }

    @Override
    protected void validate(final HodQueryRequest queryRequest) {
        assertThat(queryRequest.getSummary(), is(Summary.off.name()));
        assertThat(queryRequest.getSort(), is(Sort.relevance.name()));
    }

    @Override
    protected void validateMinimal(final HodQueryRequest queryRequest) {
        assertThat(queryRequest.getSummary(), is(Summary.off.name()));
        assertNull(queryRequest.getSort());
    }
}
