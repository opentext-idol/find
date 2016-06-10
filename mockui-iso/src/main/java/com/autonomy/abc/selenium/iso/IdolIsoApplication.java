package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.promotions.IdolPromotionService;
import com.autonomy.abc.selenium.promotions.SchedulePromotionService;
import com.autonomy.abc.selenium.users.IdolIsoUserService;
import com.hp.autonomy.frontend.selenium.control.Window;


public class IdolIsoApplication extends IsoApplication<IdolIsoElementFactory> {
    private Window window;
    private IdolIsoElementFactory factory;

    @Override
    public IdolIsoElementFactory elementFactory() {
        return factory;
    }

    @Override
    public IdolIsoApplication inWindow(Window window) {
        this.window = window;
        this.factory = new IdolIsoElementFactory(window.getSession().getDriver());
        return this;
    }

    @Override
    public IdolPromotionService promotionService() {
        return new IdolPromotionService(this);
    }

    public SchedulePromotionService schedulePromotionService(){
        return new SchedulePromotionService(this);
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
