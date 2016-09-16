package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.control.Window;

public class FindIdolFind extends IdolFind<FindIdolFindElementFactory>{

    @Override
    public Application<FindIdolFindElementFactory> inWindow(final Window window) {
        setElementFactory(new FindIdolFindElementFactory(window.getSession().getDriver()));
        return this;
    }

    public SavedSearchService savedSearchService() {
        throw new UnsupportedOperationException("Users with only the findUser role do not have a Saved Search Service");
    }
}

