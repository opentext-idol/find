package com.autonomy.abc.selenium.auth;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;

public class HsodUserBuilder {
    private String username;
    private String email;
    private Role role;
    private AuthProvider authProvider = UnknownAuth.getInstance();
    private String apiKey;
    private String domain;

    public HsodUserBuilder(final String username) {
        this.username = username;
    }

    public HsodUserBuilder(final User existingUser) {
        this(existingUser.getUsername());
        this.setRole(existingUser.getRole())
            .setAuthProvider(existingUser.getAuthProvider());
        if (existingUser instanceof HsodUser) {
            this.setHsodFields((HsodUser) existingUser);
        }
    }

    private void setHsodFields(final HsodUser user) {
        this.setEmail(user.getEmail())
            .setApiKey(user.getApiKey())
            .setDomain(user.getDomain());
    }

    public HsodUserBuilder setUsername(final String username) {
        this.username = username;
        return this;
    }

    public HsodUserBuilder setEmail(final String email) {
        this.email = email;
        return this;
    }

    public HsodUserBuilder setRole(final Role role) {
        this.role = role;
        return this;
    }

    public HsodUserBuilder setAuthProvider(final AuthProvider authProvider) {
        this.authProvider = authProvider;
        return this;
    }

    public HsodUserBuilder setApiKey(final String apiKey) {
        this.apiKey = apiKey;
        return this;
    }


    public HsodUserBuilder setDomain(final String domain) {
        this.domain = domain;
        return this;
    }

    public HsodUser build() {
        return new HsodUser(username, email, role, authProvider, apiKey, domain);
    }
}