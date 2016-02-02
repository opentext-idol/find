/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.AuthenticationInformationRetriever;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class HodAuthenticationInformationRetriever implements AuthenticationInformationRetriever<HodAuthentication> {
    @Override
    public HodAuthentication getAuthentication() {
        return (HodAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
