package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.login.AuthHasLoggedIn;
import com.autonomy.abc.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class HSOUser extends User {
    private String email;

    public HSOUser(String username, String email, Role role) {
        this(username, email, role, null);
    }

    public HSOUser(String username, String email, Role role, AuthProvider authProvider){
        super(authProvider, username, role);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void authenticate(Factory<WebDriver> driverFactory, SignupEmailHandler emailParser) {
        WebDriver driver = driverFactory.create();
        driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);

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
