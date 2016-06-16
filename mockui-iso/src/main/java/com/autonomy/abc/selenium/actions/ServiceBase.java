package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import org.openqa.selenium.WebDriver;

public abstract class ServiceBase<T extends IsoElementFactory> {
    private final IsoApplication<? extends T> application;
    private final T elementFactory;

    protected ServiceBase(final IsoApplication<? extends T> application) {
        this.application = application;
        this.elementFactory = application.elementFactory();
    }

    protected WebDriver getDriver() {
        return getElementFactory().getDriver();
    }

    protected T getElementFactory() {
        return elementFactory;
    }

    protected IsoApplication<? extends T> getApplication() {
        return application;
    }
}
