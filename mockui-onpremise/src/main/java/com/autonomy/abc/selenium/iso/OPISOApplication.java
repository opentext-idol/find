package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.promotions.IdolPromotionService;
import com.autonomy.abc.selenium.users.IdolIsoUserService;


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
    public IdolPromotionService promotionService() {
        return new IdolPromotionService(this);
    }

    @Override
    public IdolIsoUserService userService() {
        return new IdolIsoUserService(this);
    }

    @Override
    public boolean isHosted() {
        return false;
    }
}
