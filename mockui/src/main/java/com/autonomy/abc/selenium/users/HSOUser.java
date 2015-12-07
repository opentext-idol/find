package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.login.AuthHasLoggedIn;
import com.autonomy.abc.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    /**
     * TODO now you can use WebDriverFactory and create/clean up within this method probably best to actually do that
     * @param driver  MUST BE A DIFFERENT WEB DRIVER FROM THE ONE THE ADMIN IS RUNNING IN
     */
    public void resetAuthentication(WebDriver driver) {
        GMailHelper helper = new GMailHelper(driver);

        helper.goToGMail();
        helper.tryLoggingInToEmail();
        helper.waitForNewEmail();
        helper.clickUnreadMessage();
        helper.expandCollapsedMessage();

        new WebDriverWait(driver,20).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='click here']"))).click();

        try {
            new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Google")));
            verifyUser(driver);
        } catch (TimeoutException e) { /* User already verified */ }
    }

    private void verifyUser(WebDriver driver){
        driver.findElement(By.linkText("Google")).click();

        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("noAccount")));
    }

    void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void authenticate(Factory<WebDriver> driverFactory, SignupEmailHandler emailParser) {
        WebDriver driver = driverFactory.create();
        try {
            emailParser.goToUrl(driver);
            LoginPage loginPage = new HSOLoginPage(driver, new AuthHasLoggedIn(driver));
            try {
                loginPage.loginWith(getAuthProvider());
            } catch (TimeoutException e) {
                /* already signed in to auth account */
            }
        } finally {
            driver.quit();
        }
    }

}
