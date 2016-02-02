package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.users.HSODeveloperService;
import com.autonomy.abc.selenium.users.HSOUserService;

public class HSOApplication extends SearchOptimizerApplication<HSOElementFactory> {
    private Window window;
    private HSOElementFactory factory;

    @Override
    public Application<HSOElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = new HSOElementFactory(window.getSession().getDriver());
        return this;
    }

    public HSOElementFactory elementFactory() {
        return factory;
    }

    @Override
    public HSOPromotionService promotionService() {
        return new HSOPromotionService(this);
    }

    @Override
    public HSOUserService userService() {
        return new HSOUserService(this);
    }

    public ConnectionService connectionService() {
        return new ConnectionService(this);
    }

    public IndexService indexService() {
        return new IndexService(this);
    }

    public HSODeveloperService developerService() {
        return new HSODeveloperService(this);
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }
}
