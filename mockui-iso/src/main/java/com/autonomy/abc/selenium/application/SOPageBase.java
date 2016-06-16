package com.autonomy.abc.selenium.application;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class SOPageBase extends AppElement implements AppPage {
    protected SOPageBase(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    public String getPageTitle() {
        return getDriver().findElement(By.cssSelector(".page-heading .heading")).getText();
    }

    public abstract static class SOPageFactory<T extends SOPageBase> implements AppPageFactory<T> {
        private final Class<T> returnType;

        protected SOPageFactory(final Class<T> returnType) {
            this.returnType = returnType;
        }

        @Override
        public Class<T> getPageType() {
            return returnType;
        }
    }
}
