package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.page.HSOElementFactory;

public class HSODFind implements Application<HSOElementFactory> {
    private Window window;
    private HSOElementFactory factory;

    public HSODFind(Window window) {
        inWindow(window);
    }

    @Override
    public HSOElementFactory elementFactory() {
        return factory;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    @Override
    public Application<HSOElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = new HSOElementFactory(window.getSession().getDriver());
        return this;
    }
}
