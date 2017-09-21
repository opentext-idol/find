package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;

public interface IdolUserSearchService {
    UserDetails searchUser(String searchString, int startUser, int maxUsers);

    User getUserFromUid(Long uid);

    User getUserFromUsername(String username);
}
