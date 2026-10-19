/*
 * Copyright 2017, 2020 Open Text.
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

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersConfig;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersSourceConfig;
import com.hp.autonomy.frontend.find.core.configuration.UserDetailsFieldConfig;
import com.hp.autonomy.frontend.find.idol.api.UsersApi;
import com.hp.autonomy.frontend.find.idol.api.model.RelatedUser;
import com.hp.autonomy.frontend.find.idol.api.model.RelatedUsersResponse;
import com.hp.autonomy.frontend.find.idol.api.model.UsernamesResponse;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.opentext.idol.types.responses.User;
import com.opentext.idol.types.responses.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class IdolUserController implements UsersApi {

    private final ConfigService<IdolFindConfig> configService;
    private final IdolUserSearchService idolUserSearchService;

    @Autowired
    public IdolUserController(
        final ConfigService<IdolFindConfig> configService,
        final IdolUserSearchService idolUserSearchService
    ) {
        this.configService = configService;
        this.idolUserSearchService = idolUserSearchService;
    }

    /**
     * Get a list of usernames matching with names matching the search text.
     */
    @Override
    public ResponseEntity<UsernamesResponse> searchUsers(final String searchText, final Integer startUser, final Integer maxUsers) {
        // this is an important check for security reasons
        if (!configService.getConfig().getSavedSearchConfig().getSharingEnabled()) {
            throw new IllegalArgumentException("Saved search sharing is disabled");
        }
        final UserDetails details =
            idolUserSearchService.searchUser(searchText, startUser, maxUsers);
        return ResponseEntity.ok(new UsernamesResponse(details.getUser().stream()
            .map(user -> user.getUsername())
            .collect(Collectors.toList())));
    }

    /**
     * Strip a {@link RelatedUser} down to the minimal information required by consumers of the
     * {@link #getRelatedToSearch} endpoint.
     *
     * @param relatedUser
     * @param config Used to restrict the fields in the returned user
     * @return Restricted user
     */
    private RelatedUser restrictUserRelatedToSearch(
        final RelatedUser relatedUser, final RelatedUsersConfig config
    ) {
        final User user = relatedUser.getUser();
        final User restricted = new User();
        restricted.setUid(user.getUid());
        restricted.setUsername(user.getUsername());
        restricted.setEmailaddress(user.getEmailaddress());

        final Map<String, String> fields = user.getFields();
        final Map<String, String> restrictedFields = new HashMap<>();
        final RelatedUsersSourceConfig sourceConfig =
            relatedUser.getExpert() ? config.getExpertise() : config.getInterests();
        for (final UserDetailsFieldConfig allowedField :
            sourceConfig.getUserDetailsFields()
        ) {
            final String name = allowedField.getName();
            if (fields != null && fields.containsKey(name)) {
                restrictedFields.put(name, fields.get(name));
            }
        }
        restricted.setFields(restrictedFields);

        return new RelatedUser(restricted, relatedUser.getExpert());
    }

    /**
     * Get users with profiles similar to the search text.
     *
     * @return Users with only the following {@link User} fields filled in: uid, username,
     *         emailaddress, fields (restricted to configured display fields)
     */
    @Override
    public ResponseEntity<RelatedUsersResponse> getUsersRelatedToSearch(
        final String searchText,
        final Integer maxUsers
    ) {
        final RelatedUsersConfig config = configService.getConfig().getUsers().getRelatedUsers();
        // this is an important check for security reasons
        if (!config.getEnabled()) {
            throw new IllegalArgumentException("The related users feature is disabled");
        }
        return ResponseEntity.ok(new RelatedUsersResponse(idolUserSearchService.getRelatedToSearch(config, searchText, maxUsers).stream()
            .map(relatedUser -> restrictUserRelatedToSearch(relatedUser, config))
            .collect(Collectors.toList())));
    }

}
