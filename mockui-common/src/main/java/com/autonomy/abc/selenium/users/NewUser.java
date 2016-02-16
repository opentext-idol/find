package com.autonomy.abc.selenium.users;

public interface NewUser {
    User withRole(Role role);

    User replaceAuthFor(User user, UsersPage usersPage);
}
