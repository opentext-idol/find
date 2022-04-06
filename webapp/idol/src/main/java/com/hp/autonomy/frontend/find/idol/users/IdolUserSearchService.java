package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersConfig;
import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;

import java.util.List;

public interface IdolUserSearchService {
    UserDetails searchUser(String searchString, int startUser, int maxUsers);

    User getUserFromUid(Long uid);

    User getUserFromUsername(String username);

    /**
     * Get users with profiles similar to the search text.
     *
     * @param config identifies the profiles to search through
     * @param searchText
     * @param maxUsers maximum number of users to return
     * @return related users, ordered by relevance
     */
    List<RelatedUser> getRelatedToSearch(
        RelatedUsersConfig config, String searchText, int maxUsers);

}
