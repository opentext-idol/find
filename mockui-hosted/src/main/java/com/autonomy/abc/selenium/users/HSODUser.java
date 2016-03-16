package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;

public class HSODUser extends User {
    private String email;
    private String apiKey;
    private String domain;

    // TODO: properly separate required/optional params
    public HSODUser(String username, String email, Role role) {
        this(username, email, role, null);
    }

    public HSODUser(AuthProvider provider, String username, Role role) {
        this(username, null, role, provider);
    }

    public HSODUser(String username, String email, Role role, AuthProvider authProvider){
        super(authProvider, username, role);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    void setUsername(String username) {
        this.username = username;
    }

    public HSODUser withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getApiKey(){
        return apiKey;
    }

    public HSODUser withDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getDomain(){
        return domain;
    }

}
