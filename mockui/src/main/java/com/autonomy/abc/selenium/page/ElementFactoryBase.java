package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.PageMapper;
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

    protected abstract void handleSwitch(NavBarTabId tab);

    public <T extends AppPage> T switchTo(Class<T> type) {
        handleSwitch(mapper.getId(type));
        return loadPage(type);
    }

    public <T extends AppPage> T loadPage(Class<T> type) {
        return mapper.load(type, getDriver());
    }

}
