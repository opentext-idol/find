package com.autonomy.abc.selenium.application;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class SOPageBase extends AppElement implements AppPage {
    protected SOPageBase(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public abstract static class SOPageFactory<T extends SOPageBase> implements AppPageFactory<T> {
        private final Class<T> returnType;

        protected SOPageFactory(Class<T> returnType) {
            this.returnType = returnType;
        }

        public Class<T> getPageType() {
            return returnType;
        }
    }
}
