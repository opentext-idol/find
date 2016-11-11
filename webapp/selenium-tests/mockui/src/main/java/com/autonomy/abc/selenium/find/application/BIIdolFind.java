package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.BIFindService;
import com.autonomy.abc.selenium.find.numericWidgets.NumericWidgetService;
import com.autonomy.abc.selenium.find.save.SavedSearchService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.control.Window;

public class BIIdolFind extends IdolFind<BIIdolFindElementFactory>{

    @Override
    public Application<BIIdolFindElementFactory> inWindow(final Window window) {
        setElementFactory(new BIIdolFindElementFactory(window.getSession().getDriver()));
        return this;
    }

    public NumericWidgetService numericWidgetService() {
        return new NumericWidgetService(this);
    }

    public BIFindService findService() { return  new BIFindService(this);}

    public SavedSearchService savedSearchService() {
        return new SavedSearchService(this);
    }
}
