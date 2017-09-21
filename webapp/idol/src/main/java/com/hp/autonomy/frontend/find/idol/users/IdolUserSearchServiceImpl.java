package com.hp.autonomy.frontend.find.idol.users;

import com.hp.autonomy.types.idol.responses.User;
import com.hp.autonomy.types.idol.responses.UserDetails;
import com.hp.autonomy.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdolUserSearchServiceImpl implements IdolUserSearchService {

    private final UserService userService;

    @Autowired
    public IdolUserSearchServiceImpl(final UserService userService) {
        this.userService = userService;
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
}
