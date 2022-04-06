/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
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
