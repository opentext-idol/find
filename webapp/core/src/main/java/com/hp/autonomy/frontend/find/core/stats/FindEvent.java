/*
 * Copyright 2014-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.stats;

import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.NONE)
public abstract class FindEvent implements Event {

    private static final String EVENT = "Find";

    private final String search;
    private final String username;
    private final long timestamp;

    public FindEvent(final String search, final AuthenticationInformationRetriever<?, ?> authenticationInformationRetriever) {
        this.search = search;
        this.username = authenticationInformationRetriever.getPrincipal().getName();

        // TODO this might need rethinking for HOD
        timestamp = System.currentTimeMillis() / 1000L;
    }

    public String getEvent() {
        return EVENT;
    }


}
