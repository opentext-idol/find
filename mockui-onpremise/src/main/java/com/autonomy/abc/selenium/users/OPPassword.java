package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.PasswordBox;

public class OPPassword implements ReplacementAuth {
    private final String password;

    public OPPassword(String password) {
        this.password = password;
    }

    @Override
    public User replaceAuth(User toReplace) {
        return new User(new OPAccount(toReplace.getUsername(), password), toReplace.getUsername(), toReplace.getRole());
    }

    public void sendTo(PasswordBox element) {
        element.setValueAsync(password);
    }
}
