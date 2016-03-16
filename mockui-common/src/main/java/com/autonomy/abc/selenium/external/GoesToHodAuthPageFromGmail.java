package com.autonomy.abc.selenium.external;

import com.autonomy.abc.selenium.users.GoesToAuthPage;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GoesToHodAuthPageFromGmail implements GoesToAuthPage {
    private final static String GMAIL_URL = "https://accounts.google.com/ServiceLogin?service=mail&continue=https://mail.google.com/mail/#identifier";
    private final GoogleAuth googleAuth;
    private WebDriver driver;

    public GoesToHodAuthPageFromGmail(GoogleAuth auth) {
        this.googleAuth = auth;
    }

    @Override
    public void cleanUp(WebDriver driver) {
        this.driver = driver;
        goToGoogleAndLogIn();
        markAllEmailAsRead();
    }

    private void goToGoogleAndLogIn() {
        driver.get(GMAIL_URL);
        new GoogleAuth.GoogleLoginPage(driver).login(googleAuth);
    }

    private void markAllEmailAsRead() {
        openInboxOptionsDropdown();
        Waits.loadOrFadeWait();
        driver.findElement(By.xpath("//div[text()='Mark all as read']")).click();
        Waits.loadOrFadeWait();
    }

    private void openInboxOptionsDropdown(){
        driver.findElement(By.cssSelector(".T-I.J-J5-Ji.ar7.nf.T-I-ax7.L3")).click();
    }

    @Override
    public boolean tryGoingToAuthPage(WebDriver driver) {
        this.driver = driver;
        goToGoogleAndLogIn();
        while (haveNewMessages()) {
            openMessage();
            try {
                clickLinkToAuthPage();
                return true;
            } catch (NoSuchElementException e) {
                // it was probably spam
                returnToInbox();
            }
        }
        LoggerFactory.getLogger(GoesToHodAuthPageFromGmail.class)
                .info("Email failed to open; *probably* signed up already for some reason");
        return false;
    }

    private boolean haveNewMessages() {
        try {
            new WebDriverWait(driver, 60)
                    .withMessage("waiting for new email")
                    .until(new ExpectedCondition<Boolean>() {
                        @Override
                        public Boolean apply(WebDriver driver) {
                            return haveNewMessagesNow(driver);
                        }
                    });
            return true;
        } catch (TimeoutException f) {
            return false;
        }
    }

    private boolean haveNewMessagesNow(WebDriver driver) {
        List<WebElement> unreadEmails = driver.findElements(By.cssSelector(".zA.zE"));

        if (unreadEmails.size() > 0) {
            return true;
        }

        refreshInbox();
        return false;
    }

    private void openMessage(){
        openFirstUnreadMessage();
        expandCollapsedMessage();
    }

    private void refreshInbox() {
        driver.findElement(By.cssSelector(".T-I.J-J5-Ji.nu.T-I-ax7.L3")).click();
    }

    private void openFirstUnreadMessage(){
        driver.findElement(By.cssSelector(".zA.zE")).click();
    }

    private void returnToInbox(){
        driver.findElement(By.cssSelector(".T-I.J-J5-Ji.lS.T-I-ax7.ar7")).click();
    }

    private void expandCollapsedMessage() {
        try {
            new WebDriverWait(driver,10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.ajT")));

            List<WebElement> ellipses = driver.findElements(By.cssSelector("img.ajT"));
            WebElement finalEllipses = ellipses.get(ellipses.size() - 1);

            if(finalEllipses.isDisplayed()){
                finalEllipses.click();
            }
        } catch (Exception e) { /* No Ellipses */ }
    }

    private void clickLinkToAuthPage() {
        driver.findElement(By.partialLinkText("here")).click();

        Waits.loadOrFadeWait();

        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(handles.get(1));

        try {
            new WebDriverWait(driver,20).until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter")));
        } catch (TimeoutException e) {
            //If not already verified then go back to inbox
            if (!driver.getCurrentUrl().contains("already")) {
                //Want to ignore cases where users are already verified, or taken to verification       TODO figure out which cases need this to be run
                driver.close();
                driver.switchTo().window(handles.get(0));
                //Probably the wrong exception to throw but just to make things easier - happens when a link has already been used for auth
                throw new NoSuchElementException("Incorrect link clicked");
            }
        }

        driver.switchTo().window(handles.get(0));
        driver.close();
        driver.switchTo().window(handles.get(1));
    }
}
