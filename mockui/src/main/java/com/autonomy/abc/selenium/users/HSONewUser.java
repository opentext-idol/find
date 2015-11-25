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

    public HSONewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public HSONewUser(String username, String email, AuthProvider provider){
        this(username, email);
        this.provider = provider;
    }

    @Override
    public HSOUser signUpAs(Role role, UsersPage usersPage) {
        HSOUsersPage hsoUsersPage = (HSOUsersPage) usersPage;
        driver = usersPage.getDriver();

        hsoUsersPage.addUsername(username);
        hsoUsersPage.addEmail(email);
        hsoUsersPage.selectRole(role);
        hsoUsersPage.createButton().click();

        new WebDriverWait(driver,15).until(GritterNotice.notificationContaining("Created user"));

        if(hsoUsersPage.getUsernameInput().getValue().equals("")) {
            successfullyAdded(usersPage);

            return new HSOUser(username, email, role, provider);
        }

        //TODO if user hasn't been successfully added return what?
        return null;
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
        driver.get("https://accounts.google.com/ServiceLogin?service=mail&continue=https://mail.google.com/mail/#identifier");

        tryLoggingInToEmail();

        waitForNewEmail();

        //Click through into unread message
        driver.findElement(By.cssSelector(".zA.zE")).click();

        expandCollapsedMessage();

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

    private void expandCollapsedMessage() {
        try {
            WebElement ellipses = driver.findElement(By.cssSelector("img.ajT"));

            if(ellipses.isDisplayed()){
                ellipses.click();
            }
        } catch (Exception e) { /* No Ellipses */ }
    }

    private void waitForNewEmail() {
        new WebDriverWait(driver,60).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                List<WebElement> unreadEmails = driver.findElements(By.cssSelector(".zA.zE"));

                if (unreadEmails.size() > 0) {
                    return true;
                }

                driver.findElement(By.cssSelector(".T-I.J-J5-Ji.nu.T-I-ax7.L3")).click();

                return false;
            }
        });
    }

    private void tryLoggingInToEmail(){
        try {
            new FormInput(driver.findElement(By.id("Email")), driver).setAndSubmit("hodtestqa401@gmail.com");
            Thread.sleep(1000);
        } catch (Exception e) {/* Probably have had the session already open */}

        new FormInput(driver.findElement(By.id("Passwd")), driver).setAndSubmit("qoxntlozubjaamyszerfk");
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }
}
