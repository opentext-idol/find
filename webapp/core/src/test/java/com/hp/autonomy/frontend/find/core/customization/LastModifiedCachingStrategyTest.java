/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.customization;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class LastModifiedCachingStrategyTest {
    private CustomizationCachingStrategy cachingStrategy;

    @Before
    public void setUp() {
        cachingStrategy = new LastModifiedCachingStrategy();
    }

    @Test
    public void addCacheHeaders() {
        final Instant instant = Instant.ofEpochMilli(1495449747142L);
        final ResponseEntity<String> output = cachingStrategy.addCacheHeaders("cat", instant);

        assertThat(output.getStatusCode(), is(HttpStatus.OK));
        assertThat(output.getBody(), is("cat"));

        final HttpHeaders headers = output.getHeaders();
        assertThat(headers.get("Cache-Control"), hasItem(containsString("must-revalidate")));
        assertThat(headers.get("Cache-Control"), hasItem(containsString("max-age")));
        assertThat(headers.get("Last-Modified"), hasItem(containsString("Mon, 22 May 2017 10:42:27 GMT")));
    }
}
