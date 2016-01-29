package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.page.HSOElementFactory;

public class HSODFind implements Application<HSOElementFactory> {
    private final Window window;
    private final HSOElementFactory factory;

    public HSODFind(Window window) {
        this.window = window;
        this.factory = new HSOElementFactory(window.getSession().getDriver());
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
        return this;
    }
}
