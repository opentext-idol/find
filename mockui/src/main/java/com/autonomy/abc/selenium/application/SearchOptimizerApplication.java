package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.util.Factory;
import com.autonomy.abc.selenium.util.SafeClassLoader;

import java.util.EnumMap;
import java.util.Map;

public abstract class SearchOptimizerApplication<T extends SOElementFactory> implements Application<T> {
    private final static Map<ApplicationType, Factory<? extends SearchOptimizerApplication>> FACTORY_MAP = new EnumMap<>(ApplicationType.class);

    static {
        FACTORY_MAP.put(ApplicationType.HOSTED, new SafeClassLoader<>(SearchOptimizerApplication.class, "com.autonomy.abc.selenium.application.HSOApplication"));
        FACTORY_MAP.put(ApplicationType.ON_PREM, new SafeClassLoader<>(SearchOptimizerApplication.class, "com.autonomy.abc.selenium.application.OPISOApplication"));
    }

    public abstract PromotionService promotionService();

    public abstract UserService<?> userService();

    public KeywordService keywordService() {
        return new KeywordService(this);
    }

    public SearchService searchService() {
        return new SearchService(this);
    }

    public static SearchOptimizerApplication<?> ofType(ApplicationType type) {
        return FACTORY_MAP.get(type).create();
    }
}
