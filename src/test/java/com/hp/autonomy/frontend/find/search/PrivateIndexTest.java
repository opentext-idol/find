/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PrivateIndexTest {

    @Test
    public void testPrivateIndexConvertsFromJson() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        final PrivateIndex privateIndex = mapper.readValue(getClass().getResourceAsStream("/com/hp/autonomy/frontend/find/search/privateIndex.json"), PrivateIndex.class);

        assertThat(privateIndex.getFlavor(), is("standard"));
        assertThat(privateIndex.getIndex(), is("myindex"));
        assertThat(privateIndex.getType(), is("content"));
        assertThat(privateIndex.getNumComponents(), is(1));
        assertThat(privateIndex.getDateCreated(), is("Mon Mar 10 2014 19:29:47 GMT+0000 (UTC)"));
        assertThat(privateIndex.getDescription(), is(nullValue()));
        assertThat(privateIndex.getSubType(), is(nullValue()));
    }
}
