package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.BIFindService;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidgetService;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import org.openqa.selenium.WebDriver;

public class BIIdolFind extends IdolFind<BIIdolFindElementFactory>{

    public NumericWidgetService numericWidgetService() {
        return new NumericWidgetService(this);
    }

    public BIFindService findService() { return  new BIFindService(this);}

    public SavedSearchService savedSearchService() {
        return new SavedSearchService(this);
    }

    @Override
    public void withDriver(final WebDriver webDriver) {
        setElementFactory(new BIIdolFindElementFactory(webDriver));
    }
}
