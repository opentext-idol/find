package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.users.AbcHasLoggedIn;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPLoginPage extends LoginPage {

    private final WebDriver driver;

    private OPLoginPage(final WebDriver driver) {
        super(driver, new AbcHasLoggedIn(driver));

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

    public static class Factory implements ParametrizedFactory<WebDriver, OPLoginPage> {
        public OPLoginPage create(WebDriver context) {
            return new OPLoginPage(context);
        }
    }
}
