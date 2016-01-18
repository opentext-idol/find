package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.config.HSOUserConfigParser;
import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.users.HSODeveloperService;
import com.autonomy.abc.selenium.users.HSOUserService;
import org.openqa.selenium.WebDriver;

public class HSOApplication extends SearchOptimizerApplication<HSOElementFactory> {

    @Override
    public HSOElementFactory createElementFactory(WebDriver driver) {
        return new HSOElementFactory(driver);
    }

    @Override
    public HSOPromotionService createPromotionService(ElementFactory elementFactory) {
        return new HSOPromotionService(this, elementFactory);
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
