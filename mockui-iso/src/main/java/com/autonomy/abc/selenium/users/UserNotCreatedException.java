package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.users.NewUser;

public class UserNotCreatedException extends RuntimeException {
    private static final long serialVersionUID = 51059554784132489L;

    public UserNotCreatedException(final NewUser newUser){
        super(newUser + " was not created");
    }
}
