package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.ActionFactory;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;

public class SearchActionFactory extends ActionFactory {
    public SearchActionFactory(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public Search makeSearch(String searchTerm) {
        return new Search(getApplication().createAppBody(getDriver()), getElementFactory(), searchTerm);
    }
}
