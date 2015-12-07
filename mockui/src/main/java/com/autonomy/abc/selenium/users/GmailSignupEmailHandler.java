package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.page.login.GoogleAuth;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class GmailSignupEmailHandler implements SignupEmailHandler {
    private final static String GMAIL_URL = "https://accounts.google.com/ServiceLogin?service=mail&continue=https://mail.google.com/mail/#identifier";
    private final GoogleAuth googleAuth;

    public GmailSignupEmailHandler(GoogleAuth auth) {
        this.googleAuth = auth;
    }

    @Override
    public void goToUrl(WebDriver driver) {
        driver.get(GMAIL_URL);
        new GoogleAuth.GoogleLoginPage(driver).login(googleAuth);
        waitForNewEmail(driver);
        clickUnreadMessage(driver);
        expandCollapsedMessage(driver);
        clickLink(driver);
    }

    private void waitForNewEmail(WebDriver driver) {
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

    private void clickUnreadMessage(WebDriver driver){
        driver.findElement(By.cssSelector(".zA.zE")).click();
    }

    private void expandCollapsedMessage(WebDriver driver) {
        try {
            WebElement ellipses = new WebDriverWait(driver,10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.ajT")));

            if(ellipses.isDisplayed()){
                ellipses.click();
            }
        } catch (Exception e) { /* No Ellipses */ }
    }

    private void clickLink(WebDriver driver) {
        driver.findElement(By.xpath("//a[text()='here']")).click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            /* NOOP */
        }

        driver.close();
        String loginWindow = driver.getWindowHandles().toArray(new String[1])[0];
        driver.switchTo().window(loginWindow);
    }
}
