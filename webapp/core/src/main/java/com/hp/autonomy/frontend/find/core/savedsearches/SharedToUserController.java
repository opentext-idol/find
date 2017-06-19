/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@ConditionalOnProperty(BiConfiguration.BI_PROPERTY)
@RequestMapping(SharedToUserController.SHARED_SEARCHES_PATH)
class SharedToUserController {
    static final String SHARED_SEARCHES_PATH = "/api/public/search/shared-searches";
    static final String PERMISSIONS_PATH = "/permissions";
    static final String SEARCH_ID_PARAM = "searchId";
    static final String USERNAME_PARAM = "username";

    private final SharedToUserRepository sharedToUserRepository;
    private final UserEntityService userEntityService;

    @Autowired
    public SharedToUserController(
            final SharedToUserRepository sharedToUserRepository,
            final UserEntityService userEntityService
    ) {
        this.sharedToUserRepository = sharedToUserRepository;
        this.userEntityService = userEntityService;
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}", method = RequestMethod.GET)
    @ResponseBody
    public Set<SharedToUser> getPermissionsForSearch(
            @PathVariable(SEARCH_ID_PARAM) final Long searchId,
            @RequestParam(value = USERNAME_PARAM, required = false) final String username
    ) {
        return username != null
                ? sharedToUserRepository.findByUsernameAndSearchId(username, searchId)
                : sharedToUserRepository.findBySavedSearch_Id(searchId);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}", method = {RequestMethod.PUT, RequestMethod.POST})
    @ResponseBody
    public SharedToUser save(
            @RequestBody final SharedToUser sharedToUser,
            @PathVariable("searchId") final long searchId
    ) {
        final Long userId = sharedToUser.getUser().getUserId();
        final String username = sharedToUser.getUser().getUsername();

        if (userId == null) {
            final UserEntity userInDatabase = userEntityService.getOrCreate(username);

            sharedToUser.setUser(userInDatabase);
        }

        sharedToUser.setId(new SharedToUserPK(searchId, userId));

        return sharedToUserRepository.save(sharedToUser);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public SharedToUser save(
            @RequestBody final SharedToUser sharedToUser,
            @PathVariable("searchId") final long searchId,
            @PathVariable("userId") final long userId
    ) {
        sharedToUser.setId(new SharedToUserPK(searchId, userId));

        return sharedToUserRepository.save(sharedToUser);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("searchId") final long searchId,
            @PathVariable("userId") final long userId
    ) {
        sharedToUserRepository.delete(new SharedToUserPK(searchId, userId));
    }

}
