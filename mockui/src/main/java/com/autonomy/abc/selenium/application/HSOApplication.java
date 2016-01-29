package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.config.HSOUserConfigParser;
import com.autonomy.abc.selenium.config.UserConfigParser;
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
        return new HSOPromotionService(this, elementFactory());
    }

    @Override
    public HSOUserService userService(){
        return new HSOUserService(this, elementFactory());
    }

    public ConnectionService connectionService() {
        return new ConnectionService(this, elementFactory());
    }

    public IndexService indexService() {
        return new IndexService(this, elementFactory());
    }

    public HSODeveloperService developerService() {
        return new HSODeveloperService(this, elementFactory());
    }

    @Override
    public UserConfigParser getUserConfigParser() {
        return new HSOUserConfigParser();
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }
}
