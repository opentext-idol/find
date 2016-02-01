package com.autonomy.abc.selenium.page.devconsole;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DevConsoleHomePage extends AppElement implements AppPage {
    public DevConsoleHomePage(WebDriver driver) {
        super(driver.findElement(By.className("wrapper")), driver);
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".haven-splash-header-mega.wow")));
    }

    public WebElement loginButton(){
        return findElement(By.id("loginLogout"));
    }
}
