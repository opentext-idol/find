package com.autonomy.abc.selenium.navigation;

import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public abstract class ElementFactoryBase {
    private final WebDriver driver;
    private final PageMapper<?> mapper;

    protected ElementFactoryBase(WebDriver driver, PageMapper<?> mapper){
        this.driver = driver;
        this.mapper = mapper;
    }

    protected WebDriver getDriver() {
        return driver;
    }

    public <T extends AppPage> T loadPage(Class<T> type) {
        return mapper.load(type, getDriver());
    }

}
