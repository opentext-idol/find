package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.users.NewUser;

public class UserNotCreatedException extends RuntimeException {
    public UserNotCreatedException(NewUser newUser){
        super(newUser + " was not created");
    }
}
