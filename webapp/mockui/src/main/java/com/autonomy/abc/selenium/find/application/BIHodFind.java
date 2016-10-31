package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.control.Window;

public class BIHodFind extends HodFind<BIHodFindElementFactory> {
    @Override
    public Application<BIHodFindElementFactory> inWindow(Window window) {
        setElementFactory(new BIHodFindElementFactory(window.getSession().getDriver()));
        return this;
    }
}
