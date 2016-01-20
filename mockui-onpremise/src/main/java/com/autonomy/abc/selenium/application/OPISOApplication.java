package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.config.OPUserConfigParser;
import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.OPElementFactory;
import com.autonomy.abc.selenium.promotions.OPPromotionService;
import com.autonomy.abc.selenium.users.OPUserService;
import org.openqa.selenium.WebDriver;


public class OPISOApplication extends SearchOptimizerApplication<OPElementFactory> {
    @Override
    public OPElementFactory createElementFactory(WebDriver driver) {
        return new OPElementFactory(driver);
    }

    @Override
    public OPPromotionService createPromotionService(ElementFactory elementFactory) {
        return new OPPromotionService(this, elementFactory);
    }

    @Override
    public OPUserService createUserService(ElementFactory elementFactory) {
        return new OPUserService(this, elementFactory);
    }

    @Override
    public UserConfigParser getUserConfigParser() {
        return new OPUserConfigParser();
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.ON_PREM;
    }
}
