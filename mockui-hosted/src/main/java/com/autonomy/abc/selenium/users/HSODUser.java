package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

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

    @Override
    public void authenticate(Factory<WebDriver> driverFactory, SignupEmailHandler emailParser) {
        WebDriver driver = driverFactory.create();

        try {
            if(emailParser.goToUrl(driver)) {
                LoginPage loginPage = new HSOLoginPage(driver, new AuthHasLoggedIn(driver));
                try {
                    loginPage.loginWith(getAuthProvider());
                } catch (TimeoutException e) {
                /* already signed in to auth account */
                }
            }
        } finally {
            driver.quit();
        }
    }

}
