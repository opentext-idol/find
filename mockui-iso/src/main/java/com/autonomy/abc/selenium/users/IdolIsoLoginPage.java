package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.application.AppPageFactory;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IdolIsoLoginPage extends LoginPage {

    private final WebDriver driver;

    private IdolIsoLoginPage(final WebDriver driver) {
        super(driver, new SOHasLoggedIn(driver));

        this.driver = driver;

        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("button")));
    }

    public String getText() {
        return driver.findElement(By.xpath(".//*")).getText();
    }

    public WebElement usernameInput() {
        return driver.findElement(By.cssSelector("[placeholder='Username']"));
    }

    public static class Factory implements AppPageFactory<IdolIsoLoginPage> {
        @Override
        public Class<IdolIsoLoginPage> getPageType() {
            return IdolIsoLoginPage.class;
        }

        @Override
        public IdolIsoLoginPage create(final WebDriver context) {
            return new IdolIsoLoginPage(context);
        }
    }
}
