package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.users.OPUserService;


public class OPISOApplication extends SearchOptimizerApplication<OPISOElementFactory> {
    private Window window;
    private OPISOElementFactory factory;

    @Override
    public OPISOElementFactory elementFactory() {
        return factory;
    }

    @Override
    public OPISOApplication inWindow(Window window) {
        this.window = window;
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
