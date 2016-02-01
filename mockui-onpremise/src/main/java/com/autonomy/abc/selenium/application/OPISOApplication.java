package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.page.OPElementFactory;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.users.OPUserService;


public class OPISOApplication extends SearchOptimizerApplication<OPElementFactory> {
    private Window window;
    private OPElementFactory factory;

    @Override
    public OPElementFactory elementFactory() {
        return factory;
    }

    @Override
    public Application<OPElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = new OPElementFactory(window.getSession().getDriver());
        return this;
    }

    @Override
    public OPPromotionService promotionService() {
        return new OPPromotionService(this);
    }

    @Override
    public OPUserService userService() {
        return new OPUserService(this);
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.ON_PREM;
    }
}
