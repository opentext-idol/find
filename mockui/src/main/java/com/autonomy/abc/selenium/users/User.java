package com.autonomy.abc.selenium.users;

public class User {
    private final String name;
    private final String email;
    private String password;

    public User(String name, String password, String email){
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
