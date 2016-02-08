package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import org.openqa.selenium.WebDriver;

public abstract class ServiceBase<T extends SOElementFactory> {
    private final SearchOptimizerApplication<? extends T> application;
    private final T elementFactory;

    protected ServiceBase(SearchOptimizerApplication<? extends T> application) {
        this.application = application;
        this.elementFactory = application.elementFactory();
    }

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected T getElementFactory() {
        return elementFactory;
    }

    protected SearchOptimizerApplication<? extends T> getApplication() {
        return application;
    }
}
