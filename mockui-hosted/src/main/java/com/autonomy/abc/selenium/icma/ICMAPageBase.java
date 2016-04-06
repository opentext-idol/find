package com.autonomy.abc.selenium.icma;

import com.autonomy.abc.selenium.application.AppPageFactory;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public abstract class ICMAPageBase implements AppPage {
    private AppElement page;

    protected ICMAPageBase(WebDriver driver) {
        this(waitForLoad(driver), driver);
    }

    protected ICMAPageBase(WebElement element, WebDriver driver) {
        page = new AppElement(element, driver);
    }

    public AppElement getWrapperContent() {
        waitForPageLoadIndicatorToDisappear();
        return new AppElement(getDriver().findElement(By.className("wrapper-content")), getDriver());
    }

    private void waitForPageLoadIndicatorToDisappear() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("loadingIcon")));
    }

    protected AppElement getPage() {
        return page;
    }

    protected WebDriver getDriver() {
        return page.getDriver();
    }

    protected WebElement findElement(By location) {
        return page.findElement(location);
    }

    protected List<WebElement> findElements(By location) {
        return page.findElements(location);
    }

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    private static WebElement waitForLoad(WebDriver driver) {
        return new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[ui-view]")));
    }

    public abstract static class ICMAPageFactory<T extends ICMAPageBase> implements AppPageFactory<T> {
        private final Class<T> returnType;

        protected ICMAPageFactory(Class<T> returnType) {
            this.returnType = returnType;
        }

        public Class<T> getPageType() {
            return returnType;
        }
    }
}
