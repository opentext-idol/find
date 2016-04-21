/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ClickEvent extends FindEvent {

    @JsonProperty("click-type")
    private final ClickType clickType;

    public ClickEvent(final String search, final String username, final ClickType clickType) {
        super(search, username);

        this.clickType = clickType;
    }
}
