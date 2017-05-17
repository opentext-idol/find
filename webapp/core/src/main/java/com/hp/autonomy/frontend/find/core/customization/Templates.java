/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
@Data
@Builder(toBuilder = true)
public class Templates {
    private final Map<String, String> templates;
    private final Instant lastModified;
}
