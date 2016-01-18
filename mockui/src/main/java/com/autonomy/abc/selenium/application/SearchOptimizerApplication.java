package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.config.UserConfigParser;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.users.UserService;

public abstract class SearchOptimizerApplication<T extends ElementFactory> implements Application<T> {

    public abstract PromotionService createPromotionService(ElementFactory elementFactory);

    public abstract UserService createUserService(ElementFactory elementFactory);

    public abstract UserConfigParser getUserConfigParser();

    public abstract ApplicationType getType();

    public static SearchOptimizerApplication ofType(ApplicationType type) {
        return type.makeSearchApplication();
    }

    public KeywordService createKeywordService(ElementFactory elementFactory) {
        return new KeywordService(this, elementFactory);
    }

    public SearchService createSearchService(ElementFactory elementFactory) {
        return new SearchService(this, elementFactory);
    }
}
