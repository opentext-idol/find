package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.config.HSOUserConfigParser;
import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.users.HSODeveloperService;
import com.autonomy.abc.selenium.users.HSOUserService;
import org.openqa.selenium.WebDriver;

public class HSOApplication extends SearchOptimizerApplication<HSOElementFactory> {
    private Window window;
    private HSOElementFactory factory;

    @Override
    public Application<HSOElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = createElementFactory(window.getSession().getDriver());
        return this;
    }

    @Override
    public HSOElementFactory createElementFactory(WebDriver driver) {
        return new HSOElementFactory(driver);
    }

    public HSOElementFactory elementFactory() {
        return factory;
    }

    @Override
    public HSOPromotionService createPromotionService(ElementFactory elementFactory) {
        return new HSOPromotionService(this, elementFactory);
    }

    public HSOPromotionService promotionService() {
        return createPromotionService(factory);
    }

    @Override
    public HSOUserService createUserService(ElementFactory elementFactory){
        return new HSOUserService(this,elementFactory);
    }

    @Override
    public UserConfigParser getUserConfigParser() {
        return new HSOUserConfigParser();
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    public ConnectionService createConnectionService(HSOElementFactory elementFactory) {
        return new ConnectionService(this,elementFactory);
    }

    public IndexService createIndexService(HSOElementFactory elementFactory) {
        return new IndexService(this,elementFactory);
    }

    public HSODeveloperService createDeveloperService(HSOElementFactory elementFactory) {
        return new HSODeveloperService(this, elementFactory);
    }
}
