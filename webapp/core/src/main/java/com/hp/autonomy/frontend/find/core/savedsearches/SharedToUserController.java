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
import com.hp.autonomy.frontend.find.core.api.SharedToUserApi;
import com.hp.autonomy.frontend.find.core.api.model.SharedToUsersResponse;
import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnExpression(BiConfiguration.BI_PROPERTY_SPEL)
class SharedToUserController implements SharedToUserApi {
    @Autowired private ConfigService<? extends FindConfig<?, ?>> configService;
    @Autowired private SharedToUserService sharedToUserService;
    @Autowired private SharedToUserRepository sharedToUserRepository;
    @Autowired private UserEntityService userEntityService;

    private void checkEnabled() {
        if (!configService.getConfig().getSavedSearchConfig().getSharingEnabled()) {
            throw new IllegalArgumentException("Saved search sharing is disabled");
        }
    }

    @Override
    public ResponseEntity<SharedToUsersResponse> getSearchUserPermissions(
            final Long searchId,
            final String username
    ) {
        checkEnabled();
        return ResponseEntity.ok(new SharedToUsersResponse(username != null
                ? sharedToUserRepository.findByUsernameAndSearchId(username, searchId)
                : sharedToUserRepository.findBySavedSearch_Id(searchId)));
    }

    @Override
    public ResponseEntity<SharedToUser> shareSearchWithUser(
            final Long searchId,
            final SharedToUser sharedToUser
    ) {
        return updateSearchUserShare(searchId, sharedToUser);
    }

    @Override
    public ResponseEntity<SharedToUser> updateSearchUserShare(
            final Long searchId,
            final SharedToUser sharedToUser
    ) {
        checkEnabled();
        final Long userId = sharedToUser.getUser().getUserId();
        final String username = sharedToUser.getUser().getUsername();

        if (userId == null) {
            final UserEntity userInDatabase = userEntityService.getOrCreate(username);

            sharedToUser.setUser(userInDatabase);
        }

        sharedToUser.setId(new SharedToUserPK(searchId, userId));

        return ResponseEntity.ok(sharedToUserService.save(sharedToUser));
    }

    @Override
    public ResponseEntity<SharedToUser> setSearchUserPermissions(
            final Long searchId,
            final Long userId,
            final SharedToUser sharedToUser
    ) {
        checkEnabled();
        sharedToUser.setId(new SharedToUserPK(searchId, userId));

        return ResponseEntity.ok(sharedToUserService.save(sharedToUser));
    }

    @Override
    public ResponseEntity<Void> removeSearchUserShare(
            final Long searchId,
            final Long userId
    ) {
        checkEnabled();
        sharedToUserRepository.deleteById(new SharedToUserPK(searchId, userId));
        return ResponseEntity.noContent().build();
    }

}
