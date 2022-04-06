/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.customization;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
class LastModifiedCachingStrategy implements CustomizationCachingStrategy {
    private static final long MAX_AGE_DAYS = 1;

    @Override
    public <T> ResponseEntity<T> addCacheHeaders(final T body, final Instant lastModifiedTime) {
        // Browsers can cache the templates for 1 hour, then they must check the last modified time with the server
        final CacheControl cacheControl = CacheControl.maxAge(MAX_AGE_DAYS, TimeUnit.HOURS).mustRevalidate();

        return ResponseEntity
                .ok()
                .cacheControl(cacheControl)
                .lastModified(lastModifiedTime.toEpochMilli())
                .body(body);
    }
}
