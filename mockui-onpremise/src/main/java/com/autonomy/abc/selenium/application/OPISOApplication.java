package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.navigation.OPISOElementFactory;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.users.OPUserService;


public class OPISOApplication extends SearchOptimizerApplication<OPISOElementFactory> {
    private OPISOElementFactory factory;

    @Override
    public OPISOElementFactory elementFactory() {
        return factory;
    }

    @Override
    public OPISOApplication inWindow(Window window) {
        Window window1 = window;
        this.factory = new OPISOElementFactory(window.getSession().getDriver());
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
