package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.control.Window;

public class HodFind extends FindApplication<HodFindElementFactory> {
    private HodFindElementFactory factory;

    public HodFind() {
    }

    @Override
    public HodFindElementFactory elementFactory() {
        return factory;
    }

    @Override
    public boolean isHosted() {
        return true;
    }

    @Override
    public HodFind inWindow(final Window window) {
        this.factory = new HodFindElementFactory(window.getSession().getDriver());
        return this;
    }
}
