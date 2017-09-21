/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization;

import org.springframework.http.ResponseEntity;

import java.time.Instant;

public interface CustomizationCachingStrategy {
    <T> ResponseEntity<T> addCacheHeaders(T body, Instant lastModifiedTime);
}
