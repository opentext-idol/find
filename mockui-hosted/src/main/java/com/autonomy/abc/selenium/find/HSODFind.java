package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Window;

public class HSODFind implements Application<HSODFindElementFactory> {
    private Window window;
    private HSODFindElementFactory factory;

    public HSODFind(Window window) {
        inWindow(window);
    }

    @Override
    public HSODFindElementFactory elementFactory() {
        return factory;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    @Override
    public Application<HSODFindElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = new HSODFindElementFactory(window.getSession().getDriver());
        return this;
    }
}
