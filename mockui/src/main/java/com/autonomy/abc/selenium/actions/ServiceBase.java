package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import org.openqa.selenium.WebDriver;

public abstract class ServiceBase {
    private Application application;
    private ElementFactory elementFactory;

    protected ServiceBase(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected ElementFactory getElementFactory() {
        return elementFactory;
    }

    protected AppBody getBody() {
        return application.createAppBody(getDriver());
    }

    protected Application getApplication() {
        return application;
    }
}
