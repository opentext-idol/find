package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.element.PasswordBox;
import com.hp.autonomy.frontend.selenium.users.ReplacementAuth;
import com.hp.autonomy.frontend.selenium.users.User;

public class IdolIsoReplacementAuth implements ReplacementAuth {
    private final String password;

    public IdolIsoReplacementAuth(String password) {
        this.password = password;
    }

    @Override
    public User replaceAuth(User toReplace) {
        return new User(new IdolIsoAccount(toReplace.getUsername(), password), toReplace.getUsername(), toReplace.getRole());
    }

    public void sendTo(PasswordBox element) {
        element.setValueAsync(password);
    }
}
