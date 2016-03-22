package com.autonomy.abc.selenium.icma;

import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GettingStartedPage extends AppElement implements AppPage {
    private GettingStartedPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".wrapper-content:not(.page-banner)"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("video")));
    }

    private WebElement addURLInput(){
        return findElement(By.name("urlInput"));
    }

    public void addSiteToIndex(String url){
        WebElement inputBox = addURLInput();

        //Get rid of the 'http'
        inputBox.sendKeys(" ");
        inputBox.clear();

        inputBox.sendKeys(url);
        Waits.loadOrFadeWait();
        inputBox.findElement(By.xpath(".//..//i")).click();
        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Document \"" + url + "\" was uploaded successfully"));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, GettingStartedPage> {
        public GettingStartedPage create(WebDriver context) {
            return new GettingStartedPage(context);
        }
    }
}
