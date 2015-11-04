/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find;

import com.hp.autonomy.frontend.find.beanconfiguration.HodCondition;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.sso.HodAuthentication;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Conditional(HodCondition.class)
public class SystemPropertyFindQueryProfileService implements FindQueryProfileService {

    private static final String DEFAULT_QUERY_PROFILE_NAME = "search_default_profile";
    private static final String QUERY_PROFILE_NAME = System.getProperty("hp.searchoptimizer.iod.defaultqueryprofile", DEFAULT_QUERY_PROFILE_NAME);

    /**
     * Gets the name of the query profile that should be used with queries.
     * Currently either a default value or read from a system property.
     * @return  The name of the query profile to send to Haven OnDemand along with queries.
     */
    @Override
    public ResourceIdentifier getQueryProfile() {
        final String domain = ((HodAuthentication) SecurityContextHolder.getContext().getAuthentication()).getPrincipal().getApplication().getDomain();
        return new ResourceIdentifier(domain, QUERY_PROFILE_NAME);
    }
}
