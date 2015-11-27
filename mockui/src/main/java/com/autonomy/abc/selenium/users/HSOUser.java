package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

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
     *
     * @param driver  MUST BE A DIFFERENT WEB DRIVER FROM THE ONE THE ADMIN IS RUNNING IN
     */
    public void resetAuthentication(WebDriver driver) {
        GMailHelper helper = new GMailHelper(driver);

        helper.goToGMail();
        helper.tryLoggingInToEmail();
        helper.waitForNewEmail();
        helper.clickUnreadMessage();
        helper.expandCollapsedMessage();

        driver.findElement(By.xpath("//a[text()='click here']")).click();

        try {
            new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Google")));
            verifyUser(driver);
        } catch (TimeoutException e) { /* User already verified */ }

        List<String> browserHandles = new ArrayList<>(driver.getWindowHandles());

        for(int i = browserHandles.size() - 1; i >= 0; i--){
            driver.switchTo().window(browserHandles.get(i));
            driver.close();
        }

        driver.switchTo().window(browserHandles.get(0));
    }

    private void verifyUser(WebDriver driver){
        driver.findElement(By.linkText("Google")).click();

        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("noAccount")));
    }
}
