package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.promotions.HSODPromotionService;
import com.autonomy.abc.selenium.users.HSODDeveloperService;
import com.autonomy.abc.selenium.users.HSODUserService;
import com.hp.autonomy.frontend.selenium.control.Window;

public class HSODApplication extends SearchOptimizerApplication<HSODElementFactory> {
    private Window window;
    private HSODElementFactory factory;

    @Override
    public HSODApplication inWindow(Window window) {
        this.window = window;
        this.factory = new HSODElementFactory(window.getSession().getDriver());
        return this;
    }

    public HSODElementFactory elementFactory() {
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

    public ConnectionService connectionService() {
        return new ConnectionService(this);
    }

    public IndexService indexService() {
        return new IndexService(this);
    }

    public HSODDeveloperService developerService() {
        return new HSODDeveloperService(this);
    }

    @Override
    public boolean isHosted() {
        return true;
    }
}
