package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;

public abstract class UserService<T extends IsoElementFactory> extends ServiceBase<T> {
    public UserService(IsoApplication<? extends T> application) {
        super(application);
    }

    public abstract void deleteUser(User user);
    public abstract UsersPage goToUsers();
    public abstract void deleteOtherUsers();
    public abstract User changeRole(User user, Role newRole);

    public User createNewUser(NewUser newUser, Role role){
        UsersPage usersPage = goToUsers();
        usersPage.createUserButton().click();
        try {
            return usersPage.addNewUser(newUser, role);
        } finally {
            usersPage.userCreationModal().close();
        }
    }
}
