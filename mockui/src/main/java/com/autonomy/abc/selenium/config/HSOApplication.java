package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.config.authproviders.*;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOAppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.users.HSOUserService;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import com.hp.autonomy.frontend.selenium.sso.ApiKey;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
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
//        return new ApiKey(System.getProperty("com.autonomy.apiKey"));

        switch (System.getProperty("com.autonomy.loginType").toLowerCase()) {
            case ("apikey"):
                return new APIKeyInput();
            case ("google"):
                return new Google();
            case ("facebook"):
                return new Facebook();
            case ("twitter"):
                return new Twitter();
            case ("passport"):
                return new HPPassport();
            case ("yahoo"):
                return new Yahoo();
            case ("openid"):
                return new OpenID();
            default:
                return null;
        }
    }

    @Override
    public HSOPromotionService createPromotionService(ElementFactory elementFactory) {
        return new HSOPromotionService(this, elementFactory);
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    @Override
    public HSOUserService createUserService(ElementFactory elementFactory){
        return new HSOUserService(this,elementFactory);
    }
}
