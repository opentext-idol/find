package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.util.Factory;
import org.openqa.selenium.WebDriver;

public interface NewUser {
    User signUpAs(Role role, UsersPage usersPage);

    @Deprecated
    User signUpAs(Role role, UsersPage usersPage, Factory<WebDriver> webDriverFactory);

    User replaceAuthFor(User user, UsersPage usersPage);
}
