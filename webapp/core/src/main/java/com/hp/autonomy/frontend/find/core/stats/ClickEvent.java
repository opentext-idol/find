/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ClickEvent extends FindEvent {

    @JsonProperty("click-type")
    private final ClickType clickType;

    public ClickEvent(
        final String search,
        final ClickType clickType,
        final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        super(search, authenticationInformationRetriever);

        this.clickType = clickType;
    }
}
