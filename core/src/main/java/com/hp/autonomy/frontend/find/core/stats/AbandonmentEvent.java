/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AbandonmentEvent extends ClickEvent {

    private static final String TYPE = "abandonment";

    public AbandonmentEvent(final String search, final String username, final ClickType clickType) {
        super(search, username, clickType);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
