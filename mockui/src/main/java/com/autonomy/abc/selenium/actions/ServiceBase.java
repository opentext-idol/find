package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.page.ElementFactory;
import org.openqa.selenium.WebDriver;

public abstract class ServiceBase<T extends ElementFactory> {
    private SearchOptimizerApplication application;
    private T elementFactory;

    protected ServiceBase(SearchOptimizerApplication application, T elementFactory) {
        this.application = application;
        this.elementFactory = elementFactory;
    }

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected T getElementFactory() {
        return elementFactory;
    }

    protected SearchOptimizerApplication getApplication() {
        return application;
    }
}
