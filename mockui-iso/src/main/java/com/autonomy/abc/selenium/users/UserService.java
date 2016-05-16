package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;

public abstract class UserService<T extends IsoElementFactory> extends ServiceBase<T> {
    public UserService(IsoApplication<? extends T> application) {
        super(application);
    }

    public abstract UsersPage goToUsers();
    public abstract User changeRole(User user, Role newRole);
    protected abstract void deleteUserInRow(UserTableRow row);

    public User createNewUser(NewUser newUser, Role role){
        UsersPage usersPage = goToUsers();
        usersPage.createUserButton().click();
        try {
            return usersPage.addNewUser(newUser, role);
        } finally {
            usersPage.userCreationModal().close();
        }
    }

    public void deleteUser(User user){
        UsersPage<?> usersPage = goToUsers();
        deleteUserInRow(usersPage.getUserRow(user));
    }

    public void deleteOtherUsers(){
        UsersPage<?> usersPage = goToUsers();
        for (UserTableRow row : usersPage.getTable()) {
            if (row.canDeleteUser()) {
                deleteUserInRow(row);
            }
        }
    }
}
