package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.promotions.HSODPromotionService;
import com.autonomy.abc.selenium.users.HSODDeveloperService;
import com.autonomy.abc.selenium.users.HSODUserService;
import com.hp.autonomy.frontend.selenium.control.Window;

public class IsoHsodApplication extends SearchOptimizerApplication<IsoHsodElementFactory> {
    private IsoHsodElementFactory factory;

    @Override
    public IsoHsodApplication inWindow(Window window) {
        this.factory = new IsoHsodElementFactory(window.getSession().getDriver());
        return this;
    }

    public IsoHsodElementFactory elementFactory() {
        return factory;
    }

    @Override
    public HSODPromotionService promotionService() {
        return new HSODPromotionService(this);
    }

    @Override
    public HSODUserService userService() {
        return new HSODUserService(this);
    }

    public HSODDeveloperService developerService() {
        return new HSODDeveloperService(this);
    }

    @Override
    public boolean isHosted() {
        return true;
    }
}
