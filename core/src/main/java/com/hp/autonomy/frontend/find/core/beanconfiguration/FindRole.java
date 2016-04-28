/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.beanconfiguration;

public enum FindRole {
    USER,
    ADMIN,
    BI,
    CONFIG;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
