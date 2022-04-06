/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersConfig;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersSourceConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import com.hp.autonomy.searchcomponents.idol.search.QueryResponseParser;
import com.hp.autonomy.types.idol.responses.QueryResponseData;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;
import com.hp.autonomy.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class IdolUserSearchServiceImpl implements IdolUserSearchService {

    private final UserService userService;
    private final QueryResponseParser queryResponseParser;

    @Autowired
    public IdolUserSearchServiceImpl(
        final UserService userService,
        final QueryResponseParser queryResponseParser
    ) {
        this.userService = userService;
        this.queryResponseParser = queryResponseParser;
    }

    @Override
    public UserDetails searchUser(final String searchString, final int startUser, final int maxUsers) {
        return userService.searchUsers(searchString, startUser, maxUsers);
    }

    @Override
    public User getUserFromUid(final Long uid) {
        return userService.getUserDetails(uid);
    }

    @Override
    public User getUserFromUsername(final String username) {
        return userService.getUserDetails(username);
    }


    /**
     * Intermediate result for user search, wrapping up user details and search-related metadata.
     */
    @Getter
    @AllArgsConstructor
    private static class UserResult {
        private final String username;
        private final boolean expert;
        private final double weight;
    }


    /**
     * Get users with profiles similar to the search text, using only a single source of profiles.
     *
     * @param isExperts whether to tag results as experts
     */
    private List<UserResult> getRelatedToSearch(
        final RelatedUsersSourceConfig config,
        final boolean isExperts,
        final String searchText,
        final int maxUsers
    ) {
        final Set<String> usernames = new HashSet<>();
        final List<UserResult> users = new ArrayList<>();
        final int pageSize = maxUsers;
        int pageStart = 1;
        int maxResults = pageSize;

        // we can get multiple profiles for the same user, so to fill up to maxUsers, we might need
        // to check multiple pages of results
        while (usernames.size() < maxUsers) {
            final QueryResponseData responseData = userService.getRelatedToSearch(
                config.getAgentStoreProfilesDatabase(), config.getNamedArea(),
                searchText, pageStart, maxResults);
            final List<IdolSearchResult> profiles =
                queryResponseParser.parseQueryHits(responseData.getHits());

            for (final IdolSearchResult profile : profiles) {
                final FieldInfo<String> usernameField =
                    (FieldInfo<String>) profile.getFieldMap().get("USERNAME");
                final FieldInfo<String> nameField =
                    (FieldInfo<String>) profile.getFieldMap().get("NAME");
                final FieldInfo<String> field = usernameField != null ? usernameField : nameField;
                if (field != null && field.getValues().size() > 0) {
                    final String username = field.getValues().get(0).getValue();
                    if (usernames.size() < maxUsers && usernames.add(username)) {
                        users.add(new UserResult(username, isExperts, profile.getWeight()));
                    }
                }
            }

            if (profiles.size() < pageSize) {
                break;
            }
            pageStart += profiles.size();
            // maxResults is the total result set size up to this point, not the page size
            maxResults += profiles.size();
        }

        return users;
    }

    @Override
    public List<RelatedUser> getRelatedToSearch(
        final RelatedUsersConfig config,
        final String searchText,
        final int maxUsers
    ) {
        // get users from both sources
        final List<UserResult> experts =
            getRelatedToSearch(config.getExpertise(), true, searchText, maxUsers);
        final Set<String> expertNames = experts.stream()
            .map(res -> res.getUsername())
            .collect(Collectors.toSet());
        final List<UserResult> interested =
            getRelatedToSearch(config.getInterests(), false, searchText, maxUsers).stream()
                // don't include a user twice
                .filter(res -> !expertNames.contains(res.getUsername()))
                .collect(Collectors.toList());
        // merge and re-sort using relevance, continuing to respect maxUsers
        final List<UserResult> orderedUsers = Stream.concat(experts.stream(), interested.stream())
            .sorted(Comparator.<UserResult>comparingDouble(res -> res.getWeight()).reversed())
            .limit(maxUsers)
            .collect(Collectors.toList());

        final List<User> users = userService.getUsersDetails(
            orderedUsers.stream().map(res -> res.getUsername()).collect(Collectors.toList())
        );

        // handle getUsersDetails reordering or omitting users
        final Map<String, User> usersByName = users.stream()
            .collect(Collectors.toMap(user -> user.getUsername(), user -> user));
        return orderedUsers.stream()
            .filter(res -> usersByName.containsKey(res.getUsername()))
            .map(res -> new RelatedUser(usersByName.get(res.getUsername()), res.isExpert()))
            .collect(Collectors.toList());
    }

}
