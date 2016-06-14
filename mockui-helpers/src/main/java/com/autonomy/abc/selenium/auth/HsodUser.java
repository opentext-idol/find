package com.autonomy.abc.selenium.auth;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;

public class HsodUser extends User {
    private final String email;
    private final String apiKey;
    private final String domain;

    HsodUser(final String username, final String email, final Role role, final AuthProvider provider, final String apikey, final String domain) {
        super(provider, username, role);
        this.email = email;
        this.apiKey = apikey;
        this.domain = domain;
    }

    public String getEmail() {
        return email;
    }

    public String getApiKey(){
        return apiKey;
    }

    public String getDomain(){
        return domain;
    }

}
