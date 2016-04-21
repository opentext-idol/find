/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageEvent extends FindEvent {

    private static final String TYPE = "page";

    private final int page;

    public PageEvent(final String search, final String username, final int page) {
        super(search, username);
        this.page = page;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
