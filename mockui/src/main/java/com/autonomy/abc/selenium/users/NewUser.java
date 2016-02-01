package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;

public interface NewUser {
    User withRole(Role role);

    User replaceAuthFor(User user, UsersPage usersPage);
}
