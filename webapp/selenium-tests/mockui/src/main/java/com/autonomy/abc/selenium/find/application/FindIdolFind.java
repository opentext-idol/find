package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.save.SavedSearchService;
import org.openqa.selenium.WebDriver;

public class FindIdolFind extends IdolFind<FindIdolFindElementFactory>{

    FindIdolFind(){}

    public SavedSearchService savedSearchService() {
        throw new UnsupportedOperationException("Users with only the findUser role do not have a Saved Search Service");
    }

    @Override
    public void withDriver(final WebDriver webDriver) {
        setElementFactory(new FindIdolFindElementFactory(webDriver));
    }
}

