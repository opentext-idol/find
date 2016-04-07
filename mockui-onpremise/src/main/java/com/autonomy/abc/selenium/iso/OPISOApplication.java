package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.users.OPUserService;


public class OPISOApplication extends SearchOptimizerApplication<IdolIsoElementFactory> {
    private Window window;
    private IdolIsoElementFactory factory;

    @Override
    public IdolIsoElementFactory elementFactory() {
        return factory;
    }

    @Override
    public OPISOApplication inWindow(Window window) {
        this.window = window;
        this.factory = new IdolIsoElementFactory(window.getSession().getDriver());
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
    public boolean isHosted() {
        return false;
    }
}
