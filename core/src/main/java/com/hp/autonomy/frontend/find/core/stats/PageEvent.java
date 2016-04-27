/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hp.autonomy.searchcomponents.core.authentication.AuthenticationInformationRetriever;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(PageEvent.TYPE)
@JsonDeserialize()
public class PageEvent extends FindEvent {

    static final String TYPE = "page";

    private final int page;

    public PageEvent(
        @JsonProperty("search") final String search,
        @JsonProperty("page") final int page,
        @JacksonInject final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        super(search, authenticationInformationRetriever);
        this.page = page;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
