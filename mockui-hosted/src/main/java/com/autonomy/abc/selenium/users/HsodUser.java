package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;

public class HsodUser extends User {
    private String email;
    private String apiKey;
    private String domain;

    HsodUser(String username, String email, Role role, AuthProvider provider, String apikey, String domain) {
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
