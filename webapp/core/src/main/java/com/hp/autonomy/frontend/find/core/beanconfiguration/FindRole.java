/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

public enum FindRole {
    USER,
    ADMIN,
    BI,
    CONFIG;

    public static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
