package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.hsod.IsoHsodApplication;
import com.autonomy.abc.selenium.iso.IdolIsoApplication;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.users.UserService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.util.AppPage;

public abstract class IsoApplication<T extends IsoElementFactory> implements Application<T> {
    private LoginService loginService;

    public abstract PromotionService promotionService();

    public abstract UserService<?> userService();

    public KeywordService keywordService() {
        return new KeywordService(this);
    }

    public SearchService searchService() {
        return new SearchService(this);
    }

    @Override
    public LoginService loginService() {
        if (loginService == null) {
            loginService = new LoginService(this);
        }
        return loginService;
    }

    public <S extends AppPage> S switchTo(Class<S> pageType) {
        elementFactory().handleSwitch(pageType);
        return elementFactory().loadPage(pageType);
    }

    @Override
    public String getName() {
        return "Search";
    }

    public static IsoApplication<?> ofType(ApplicationType type) {
        switch (type) {
            case HOSTED:
                return new IsoHsodApplication();
            case ON_PREM:
                return new IdolIsoApplication();
            default:
                throw new IllegalStateException("Unsupported app type: " + type);
        }
    }

    @Override
    public abstract IsoApplication<T> inWindow(Window window);
}
