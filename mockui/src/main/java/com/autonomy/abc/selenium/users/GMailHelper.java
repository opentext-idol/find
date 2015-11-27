package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.FormInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class GMailHelper {

    private final WebDriver driver;

    public GMailHelper(WebDriver driver){
        this.driver = driver;
    }

    public void goToGMail(){
        driver.get("https://accounts.google.com/ServiceLogin?service=mail&continue=https://mail.google.com/mail/#identifier");
    }

    public void tryLoggingInToEmail(){
        try {
            new FormInput(driver.findElement(By.id("Email")), driver).setAndSubmit("hodtestqa401@gmail.com");
            Thread.sleep(1000);
        } catch (Exception e) {/* Probably have had the session already open */}

        new FormInput(driver.findElement(By.id("Passwd")), driver).setAndSubmit("qoxntlozubjaamyszerfk");
    }

    public void expandCollapsedMessage() {
        try {
            WebElement ellipses = new WebDriverWait(driver,10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.ajT")));

            if(ellipses.isDisplayed()){
                ellipses.click();
            }
        } catch (Exception e) { /* No Ellipses */ }
    }

    public void waitForNewEmail() {
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

    public void clickUnreadMessage(){
        driver.findElement(By.cssSelector(".zA.zE")).click();
    }

}
