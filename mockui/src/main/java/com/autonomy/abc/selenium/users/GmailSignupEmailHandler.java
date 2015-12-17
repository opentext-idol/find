package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.login.GoogleAuth;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class GmailSignupEmailHandler implements SignupEmailHandler {
    private final static String GMAIL_URL = "https://accounts.google.com/ServiceLogin?service=mail&continue=https://mail.google.com/mail/#identifier";
    private final GoogleAuth googleAuth;
    private WebDriver driver;

    public GmailSignupEmailHandler(GoogleAuth auth) {
        this.googleAuth = auth;
    }

    @Override
    public void goToUrl(WebDriver driver) {
        this.driver = driver;
        driver.get(GMAIL_URL);
        new GoogleAuth.GoogleLoginPage(driver).login(googleAuth);
        openMessage();
        try {
            clickLink();
        } catch (NoSuchElementException e) {
            /* Probably had an unread email */

            driver.findElement(By.cssSelector(".T-I.J-J5-Ji.lS.T-I-ax7.ar7")).click();
            openMessage();
            clickLink();
        }
    }

    private void openMessage(){
        waitForNewEmail();
        clickUnreadMessage();
        expandCollapsedMessage();
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

    private void clickUnreadMessage(){
        driver.findElement(By.cssSelector(".zA.zE")).click();
    }

    private void expandCollapsedMessage() {
        try {
            WebElement ellipses = new WebDriverWait(driver,10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.ajT")));

            if(ellipses.isDisplayed()){
                ellipses.click();
            }
        } catch (Exception e) { /* No Ellipses */ }
    }

    private void clickLink() {
        driver.findElement(By.partialLinkText("here")).click();

        loadOrFadeWait();

        //Want to ignore cases where users are already verified, or taken to verification       TODO figure out which cases need this to be run
        if(!driver.getCurrentUrl().contains("/verification/")){
            List<String> handles = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(handles.get(1));
            driver.close();
            driver.switchTo().window(handles.get(0));
            //Probably the wrong exception to throw but just to make things easier - happens when a link has already been used for auth
            throw new NoSuchElementException("Incorrect link clicked");
        }

        driver.close();
        String loginWindow = driver.getWindowHandles().toArray(new String[1])[0];
        driver.switchTo().window(loginWindow);
    }

    private void loadOrFadeWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            /* NOOP */
        }
    }
}
