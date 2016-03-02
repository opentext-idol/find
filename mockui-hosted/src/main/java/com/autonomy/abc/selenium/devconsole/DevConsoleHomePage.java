package com.autonomy.abc.selenium.devconsole;

import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DevConsoleHomePage extends AppElement implements AppPage {
    private DevConsoleHomePage(WebDriver driver) {
        super(driver.findElement(By.className("wrapper")), driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".haven-splash-header-mega.wow")));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, DevConsoleHomePage> {
        public DevConsoleHomePage create(WebDriver context) {
            return new DevConsoleHomePage(context);
        }
    }
}
