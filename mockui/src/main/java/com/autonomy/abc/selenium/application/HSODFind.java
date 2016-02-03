package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.page.HSODElementFactory;

public class HSODFind implements Application<HSODElementFactory> {
    private Window window;
    private HSODElementFactory factory;

    public HSODFind(Window window) {
        inWindow(window);
    }

    @Override
    public HSODElementFactory elementFactory() {
        return factory;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    @Override
    public Application<HSODElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = new HSODElementFactory(window.getSession().getDriver());
        return this;
    }
}
