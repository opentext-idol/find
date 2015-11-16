package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOAppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.users.HSOUserService;
import org.openqa.selenium.WebDriver;

public class HSOApplication extends Application {
    @Override
    public AppBody createAppBody(WebDriver driver) {
        return new HSOAppBody(driver);
    }

    @Override
    public HSOAppBody createAppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar) {
        return new HSOAppBody(driver, topNavBar, sideNavBar);
    }

    @Override
    public ElementFactory createElementFactory(WebDriver driver) {
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
}
