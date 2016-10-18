package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.control.Window;

public class FindHodFind extends HodFind<FindHodFindElementFactory> {

    public FindHodFind() {
    }

    @Override
    public FindHodFind inWindow(final Window window) {
        setElementFactory(new FindHodFindElementFactory(window.getSession().getDriver()));
        return this;
    }
}
