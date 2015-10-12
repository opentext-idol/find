package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOAppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.ApiKey;
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
    public AuthProvider createCredentials() {
        return new ApiKey(System.getProperty("com.autonomy.apiKey"));
    }

    @Override
    public HSOPromotionService createPromotionService(ElementFactory elementFactory) {
        return new HSOPromotionService(this, elementFactory);
    }
}
