/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.xml.bind.annotation.XmlElement;

@Data
@RequiredArgsConstructor(access = AccessLevel.NONE)
public abstract class FindEvent implements Event {

    private static final String EVENT = "Find";

    private final String search;
    private final String username;
    private final long timestamp;

    public FindEvent(final String search, final String username) {
        this.search = search;
        this.username = username;

        // TODO this might need rethinking for HOD
        timestamp = System.currentTimeMillis() / 1000L;
    }

    public String getEvent() {
        return EVENT;
    }


}
