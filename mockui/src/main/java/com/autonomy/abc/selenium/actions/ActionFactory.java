package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.search.Search;
import org.openqa.selenium.WebDriver;

public class ActionFactory {
    private Application application;
    private WebDriver driver;
    private ElementFactory elementFactory;

    public ActionFactory(Application application, ElementFactory elementFactory) {
        this.application = application;
        this.driver = elementFactory.getDriver();
        this.elementFactory = elementFactory;
    }

    public Search createSearch(String searchTerm) {
        return new Search(application.createAppBody(driver), elementFactory, searchTerm);
    }
}
