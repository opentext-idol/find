package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.hp.autonomy.frontend.selenium.login.AuthProvider;
import org.openqa.selenium.WebDriver;

public abstract class Application {
    public abstract AppBody createAppBody(WebDriver driver);

    public abstract AppBody createAppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar);

    public abstract ElementFactory createElementFactory(WebDriver driver);
    public abstract AuthProvider createCredentials();

    public PromotionService createPromotionService(ElementFactory elementFactory) {
        return new PromotionService(this, elementFactory);
    }

    // TODO: this still does not allow complete separation
    public static Application ofType(ApplicationType type) {
        return (type == ApplicationType.HOSTED) ? new HSOApplication() : new OPApplication();
    };
    
    public abstract ApplicationType getType();
}
