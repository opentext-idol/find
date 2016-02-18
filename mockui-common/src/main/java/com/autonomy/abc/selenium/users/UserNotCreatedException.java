package com.autonomy.abc.selenium.users;

public class UserNotCreatedException extends RuntimeException {
    public UserNotCreatedException(NewUser newUser){
        super(newUser + " was not created");
    }
}
