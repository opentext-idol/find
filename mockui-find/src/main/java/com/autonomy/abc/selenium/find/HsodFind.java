package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.hp.autonomy.frontend.selenium.control.Window;

public class HsodFind extends FindApplication<HsodFindElementFactory> {
    private Window window;
    private HsodFindElementFactory factory;

    public HsodFind() {
    }

    public HsodFind(Window window) {
        inWindow(window);
    }

    @Override
    public HsodFindElementFactory elementFactory() {
        return factory;
    }

    @Override
    public boolean isHosted() {
        return true;
    }

    @Override
    public HsodFind inWindow(Window window) {
        this.window = window;
        this.factory = new HsodFindElementFactory(window.getSession().getDriver());
        return this;
    }
}
