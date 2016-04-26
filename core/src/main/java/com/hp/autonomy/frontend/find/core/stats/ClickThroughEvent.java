/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(ClickThroughEvent.TYPE)
public class ClickThroughEvent extends ClickEvent {

    static final String TYPE = "clickthrough";

    private final int position;

    public ClickThroughEvent(
        @JsonProperty("search") final String search,
        @JsonProperty("username") final String username,
        @JsonProperty("click-type") final ClickType clickType,
        @JsonProperty("position") final int position
    ) {
        super(search, username, clickType);
        this.position = position;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
