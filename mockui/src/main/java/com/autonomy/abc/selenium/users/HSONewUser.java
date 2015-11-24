package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

// TODO: CSA-1663
public class HSONewUser implements NewUser {

    private final String username;
    private final String email;

    public HSONewUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @Override
    public HSOUser signUpAs(Role role, UsersPage usersPage) {
        HSOUsersPage hsoUsersPage = (HSOUsersPage) usersPage;

        hsoUsersPage.addUsername(username);
        hsoUsersPage.addEmail(email);
        hsoUsersPage.selectRole(role);
        hsoUsersPage.createButton().click();
        hsoUsersPage.loadOrFadeWait();

        WebElement verificationPage = getGmail(usersPage);
        verifyUser(verificationPage);

        return new HSOUser(username,email,role);
    }

    List<String> browserHandles;

    private WebElement getGmail(UsersPage usersPage){
        browserHandles = usersPage.createAndListWindowHandles();

        WebDriver driver = usersPage.getDriver();

        driver.switchTo().window(browserHandles.get(1));
        driver.get("https://accounts.google.com/ServiceLogin?service=mail&continue=https://mail.google.com/mail/#identifier");

        new FormInput(driver.findElement(By.id("Email")), driver).setAndSubmit("hodtestqa401@gmail.com");
        new FormInput(driver.findElement(By.id("Passwd")), driver).setAndSubmit("qoxntlozubjaamyszerfk");

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

        driver.findElement(By.cssSelector(".zA.zE")).click();

        try {
            WebElement elipses = driver.findElement(By.cssSelector("img.ajT"));

            if(elipses.isDisplayed()){
                elipses.click();
            }
        } catch (Exception e) { /* No Elipses */ }

        driver.findElement(By.xpath("//a[text()='here']")).click();

        new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Google")));

        browserHandles = new ArrayList<>(driver.getWindowHandles());

        return driver.findElement(By.tagName("body"));
    }

    private void verifyUser(WebElement verificationPage){
        verificationPage.findElement(By.linkText("Google")).click();
    }

    @Override
    public User replaceAuthFor(User user, UsersPage usersPage) {
        return null;
    }
}
