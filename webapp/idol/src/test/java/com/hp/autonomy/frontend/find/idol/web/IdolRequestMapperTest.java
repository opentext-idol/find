/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.core.web.RequestMapper;
import com.hp.autonomy.frontend.find.core.web.RequestMapperTest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class IdolRequestMapperTest extends RequestMapperTest<String> {
    @Override
    protected RequestMapper<String> constructRequestMapper() {
        return new IdolRequestMapper();
    }

    @Override
    protected String completeJsonObject() throws IOException {
        return IOUtils.toString(IdolRequestMapperTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/web/search-request.json"));
    }

    @Override
    protected String minimalJsonObject() throws IOException {
        return IOUtils.toString(IdolRequestMapperTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/idol/web/search-request-minimal.json"));
    }

    @Override
    protected void validateDatabases(final List<String> databases) {
        assertThat(databases, hasItem(is("ClassicalLiterature")));
        assertThat(databases, hasItem(is("EpicLiterature")));
    }
}
