/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
package com.hp.autonomy.frontend.find;

import org.springframework.stereotype.Service;

@Service
public class SystemPropertyFindQueryProfileService implements FindQueryProfileService {

    private final String DEFAULT_QUERY_PROFILE_NAME = "search_default_profile";

    /**
     * Gets the name of the query profile that should be used with queries.
     * Currently either a default value or read from a system property.
     * @return  The name of the query profile to send to Haven OnDemand along with queries.
     */
    @Override
    public String getQueryProfile() {
        return System.getProperty("hp.searchoptimizer.iod.defaultqueryprofile", DEFAULT_QUERY_PROFILE_NAME);
    }
}
