/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.core.web.RequestMapperTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class HodRequestMapperTest extends RequestMapperTest<ResourceIdentifier> {
    @Override
    protected RequestMapper<ResourceIdentifier> constructRequestMapper() {
        return new HodRequestMapper();
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
    protected void validateDatabases(final List<ResourceIdentifier> databases) {
        assertThat(databases, hasItem(is(new ResourceIdentifier("ClassicalDomain", "ClassicalLiterature"))));
        assertThat(databases, hasItem(is(new ResourceIdentifier("ClassicalDomain", "EpicLiterature"))));
    }
}
