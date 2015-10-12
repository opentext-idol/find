package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.ActionFactory;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;

public class SearchActionFactory extends ActionFactory {
    // TODO: does this go stale?
    private AppBody body;

    public SearchActionFactory(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
        body = application.createAppBody(getDriver());
    }

    public Search makeSearch(String searchTerm) {
        return new Search(body, getElementFactory(), searchTerm);
    }
}
