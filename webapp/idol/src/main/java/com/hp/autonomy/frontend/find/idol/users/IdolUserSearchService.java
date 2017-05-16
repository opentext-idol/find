package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.types.idol.responses.UserDetails;

@FunctionalInterface
public interface IdolUserSearchService {
    UserDetails searchUser(String searchString, int startUser, int maxUsers);
}
