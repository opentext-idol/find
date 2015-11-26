package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: CSA-1663
public class HSONewUser implements NewUser {

    private final String username;
    private final String email;
    private AuthProvider provider;
    private boolean validate = false;

    public HSONewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public HSONewUser(String username, String email, AuthProvider provider){
        this(username, email);
        this.provider = provider;
    }

    public HSONewUser(String username, String email, AuthProvider provider, boolean validate){
        this(username, email, provider);
        this.validate = validate;
    }

    public HSONewUser validate(){
        validate = true;
        return this;
    }

    @Override
    public HSOUser signUpAs(Role role, UsersPage usersPage) {
        HSOUsersPage hsoUsersPage = (HSOUsersPage) usersPage;
        driver = usersPage.getDriver();

        hsoUsersPage.addUsername(username);
        hsoUsersPage.addEmail(email);
        hsoUsersPage.selectRole(role);
        hsoUsersPage.createButton().click();

        new WebDriverWait(driver,15).withMessage("User hasn't been created").until(GritterNotice.notificationContaining("Created user"));

        hsoUsersPage.loadOrFadeWait();

        if (hsoUsersPage.getUsernameInput().getValue().equals("")) {
            if(validate) {
                successfullyAdded(usersPage);
            }

            return new HSOUser(username, email, role, provider);
        }

        throw new UserNotCreatedException(this);
    }

    private void successfullyAdded(UsersPage usersPage) {
        browserHandles = usersPage.createAndListWindowHandles();

        getGmail();

        try {
            new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Google")));
            verifyUser();
        } catch (TimeoutException e) { /* User already verified */ }

        for(int i = driver.getWindowHandles().size() - 1; i > 0; i--){
            driver.switchTo().window(browserHandles.get(i));
            driver.close();
        }

        driver.switchTo().window(browserHandles.get(0));
    }

    List<String> browserHandles;
    WebDriver driver;

    private void getGmail(){
        driver.switchTo().window(browserHandles.get(1));

        GMailHelper helper = new GMailHelper(driver);

        helper.goToGMail();
        helper.tryLoggingInToEmail();
        helper.waitForNewEmail();
        helper.clickUnreadMessage();
        helper.expandCollapsedMessage();

        //Click on link to verify
        driver.findElement(By.xpath("//a[text()='here']")).click();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {/**/}

        browserHandles = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(browserHandles.get(2));
    }

    private void verifyUser(){
        driver.findElement(By.linkText("Google")).click();

        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("noAccount")));
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }

    public class UserNotCreatedException extends RuntimeException {
        public UserNotCreatedException(HSONewUser user){
            this(user.username);
        }

        public UserNotCreatedException(String username){
            super("User '" + username + "' was not created");
        }
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public AuthProvider getProvider() {
        return provider;
    }
}
