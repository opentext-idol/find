package com.autonomy.abc.selenium.auth;

import com.hp.autonomy.frontend.selenium.element.PasswordBox;
import com.hp.autonomy.frontend.selenium.users.ReplacementAuth;
import com.hp.autonomy.frontend.selenium.users.User;

public class IdolIsoReplacementAuth implements ReplacementAuth {
    private final String password;

    public IdolIsoReplacementAuth(final String password) {
        this.password = password;
    }

    @Override
    public User replaceAuth(final User toReplace) {
        return new User(new IdolIsoAccount(toReplace.getUsername(), password), toReplace.getUsername(), toReplace.getRole());
    }

    public void sendTo(final PasswordBox element) {
        element.setValueAsync(password);
    }
}
