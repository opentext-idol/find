/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

public enum FindRole {
    USER,
    ADMIN,
    BI,
    RATING,
    CONFIG;

    public static final String HAS_RATING_ROLE = "hasRole('ROLE_RATING')";

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
