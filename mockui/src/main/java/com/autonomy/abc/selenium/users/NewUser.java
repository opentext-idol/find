package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;

public interface NewUser {
    User convertToUser();
    void signup(UsersPage usersPage);
}
