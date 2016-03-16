package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.application.SOElementFactory;

public abstract class UserService<T extends SOElementFactory> extends ServiceBase<T> {

    protected UsersPage usersPage;

    public UserService(SearchOptimizerApplication<? extends T> application) {
        super(application);
    }

    public abstract void deleteUser(User user);
    public abstract UsersPage goToUsers();
    public abstract void deleteOtherUsers();
    public abstract User changeRole(User user, Role newRole);

    public UsersPage getUsersPage() {
        return usersPage;
    }

    public void setUsersPage(UsersPage usersPage) {
        this.usersPage = usersPage;
    }

    public User createNewUser(NewUser newUser, Role role){
        usersPage = goToUsers();
        usersPage.createUserButton().click();
        try {
            return usersPage.addNewUser(newUser, role);
        } finally {
            usersPage.closeModal();
        }
    }
}
