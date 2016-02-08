package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import com.autonomy.abc.selenium.page.admin.UsersPage;

public abstract class UserService<T extends SOElementFactory> extends ServiceBase<T> {

    protected UsersPage usersPage;

    public UserService(SearchOptimizerApplication<? extends T> application) {
        super(application);
    }

    public abstract void deleteUser(User user);
    public abstract UsersPage goToUsers();
    public abstract User createNewUser(NewUser newUser, Role role);
    public abstract void deleteOtherUsers();
    public abstract User changeRole(User user, Role newRole);

    public UsersPage getUsersPage() {
        return usersPage;
    }

    public void setUsersPage(UsersPage usersPage) {
        this.usersPage = usersPage;
    }
}
