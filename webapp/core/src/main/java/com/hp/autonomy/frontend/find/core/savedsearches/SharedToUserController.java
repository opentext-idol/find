/*
 * Copyright 2018 Open Text.
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

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
@RequestMapping(SharedToUserController.SHARED_SEARCHES_PATH)
class SharedToUserController {
    static final String SHARED_SEARCHES_PATH = "/api/public/search/shared-searches";
    static final String PERMISSIONS_PATH = "/permissions";
    static final String SEARCH_ID_PARAM = "searchId";
    private static final String USERNAME_PARAM = "username";

    @Autowired private ConfigService<? extends FindConfig<?, ?>> configService;
    @Autowired private SharedToUserService sharedToUserService;
    @Autowired private SharedToUserRepository sharedToUserRepository;
    @Autowired private UserEntityService userEntityService;

    private void checkEnabled() {
        if (!configService.getConfig().getSavedSearchConfig().getSharingEnabled()) {
            throw new IllegalArgumentException("Saved search sharing is disabled");
        }
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}", method = RequestMethod.GET)
    @ResponseBody
    public Set<SharedToUser> getPermissionsForSearch(
            @PathVariable(SEARCH_ID_PARAM) final Long searchId,
            @RequestParam(value = USERNAME_PARAM, required = false) final String username
    ) {
        checkEnabled();
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
        checkEnabled();
        final Long userId = sharedToUser.getUser().getUserId();
        final String username = sharedToUser.getUser().getUsername();

        if (userId == null) {
            final UserEntity userInDatabase = userEntityService.getOrCreate(username);

            sharedToUser.setUser(userInDatabase);
        }

        sharedToUser.setId(new SharedToUserPK(searchId, userId));

        return sharedToUserService.save(sharedToUser);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public SharedToUser save(
            @RequestBody final SharedToUser sharedToUser,
            @PathVariable("searchId") final long searchId,
            @PathVariable("userId") final long userId
    ) {
        checkEnabled();
        sharedToUser.setId(new SharedToUserPK(searchId, userId));

        return sharedToUserService.save(sharedToUser);
    }

    @RequestMapping(value = PERMISSIONS_PATH + "/{searchId}/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("searchId") final long searchId,
            @PathVariable("userId") final long userId
    ) {
        checkEnabled();
        sharedToUserRepository.deleteById(new SharedToUserPK(searchId, userId));
    }

}
