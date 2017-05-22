/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
