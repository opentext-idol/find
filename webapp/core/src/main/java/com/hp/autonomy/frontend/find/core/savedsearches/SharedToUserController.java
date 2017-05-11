/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
@RequestMapping(SharedToUserController.SHARED_SEARCHES_PATH)
class SharedToUserController {
    static final String SHARED_SEARCHES_PATH = "/api/public/search/shared-searches";
    private static final String PERMISSIONS_FOR_SEARCH_PATH = "permissions-for-search";
    private static final String SEARCH_ID_PARAM = "searchId";

    private final SharedToUserRepository sharedToUserRepository;

    @Autowired
    public SharedToUserController(final SharedToUserRepository sharedToUserRepository) {
        this.sharedToUserRepository = sharedToUserRepository;
    }

    @RequestMapping(PERMISSIONS_FOR_SEARCH_PATH)
    public Set<SharedToUser> getPermittedUsersForSearch(@RequestParam(SEARCH_ID_PARAM) final String searchId) {
        return sharedToUserRepository.findBySavedSearch_Id(Long.parseLong(searchId));
    }
}
