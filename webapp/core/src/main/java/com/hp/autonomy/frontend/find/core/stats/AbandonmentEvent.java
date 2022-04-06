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
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName(AbandonmentEvent.TYPE)
public class AbandonmentEvent extends ClickEvent {

    static final String TYPE = "abandonment";

    public AbandonmentEvent(
        @JsonProperty("search") final String search,
        @JsonProperty("click-type") final ClickType clickType,
        @JacksonInject final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever
    ) {
        super(search, clickType, authenticationInformationRetriever);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
