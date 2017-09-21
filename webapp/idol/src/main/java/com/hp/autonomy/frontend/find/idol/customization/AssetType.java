/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum AssetType {
    BIG_LOGO("big-logo", "/static/img/Find_Logo_lge.png"),
    SMALL_LOGO("small-logo", "/static/img/Find_Logo_sml.png");

    private final String directory;
    private final String defaultValue;
}
