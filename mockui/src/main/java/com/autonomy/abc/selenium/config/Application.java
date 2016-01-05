package com.autonomy.abc.selenium.config;

import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.users.UserService;
import org.openqa.selenium.WebDriver;

public abstract class Application {
    public abstract AppBody createAppBody(WebDriver driver);

    public abstract AppBody createAppBody(WebDriver driver, TopNavBar topNavBar, SideNavBar sideNavBar);

    public abstract ElementFactory createElementFactory(WebDriver driver);

    public abstract PromotionService createPromotionService(ElementFactory elementFactory);

    public abstract UserService createUserService(ElementFactory elementFactory);

    public abstract UserConfigParser getUserConfigParser();

    public abstract ApplicationType getType();

    public static Application ofType(ApplicationType type) {
        return type.makeApplication();
    }

    public KeywordService createKeywordService(ElementFactory elementFactory) {
        return new KeywordService(this, elementFactory);
    }

    public SearchService createSearchService(ElementFactory elementFactory) {
        return new SearchService(this, elementFactory);
    }
}
