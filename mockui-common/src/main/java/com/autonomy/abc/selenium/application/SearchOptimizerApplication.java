package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.users.UserService;
import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.Factory;
import com.hp.autonomy.frontend.selenium.util.SafeClassLoader;

import java.util.EnumMap;
import java.util.Map;

public abstract class SearchOptimizerApplication<T extends SOElementFactory> implements Application<T> {
    private final static Map<ApplicationType, Factory<? extends SearchOptimizerApplication>> FACTORY_MAP = new EnumMap<>(ApplicationType.class);
    private LoginService loginService;

    static {
        FACTORY_MAP.put(ApplicationType.HOSTED, new SafeClassLoader<>(SearchOptimizerApplication.class, "com.autonomy.abc.selenium.hsod.HSODApplication"));
        FACTORY_MAP.put(ApplicationType.ON_PREM, new SafeClassLoader<>(SearchOptimizerApplication.class, "com.autonomy.abc.selenium.iso.IdolIsoApplication"));
    }

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

    public static SearchOptimizerApplication<?> ofType(ApplicationType type) {
        return FACTORY_MAP.get(type).create();
    }

    @Override
    public abstract SearchOptimizerApplication<T> inWindow(Window window);
}
