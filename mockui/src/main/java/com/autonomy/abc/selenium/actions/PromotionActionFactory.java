package com.autonomy.abc.selenium.actions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.Search;

import java.util.List;

// TODO: deprecate?
// may be useful for e.g. exploratory tests, but
// for most cases PromotionService is more useful
public class PromotionActionFactory extends ActionFactory {
    private PromotionService promotionService;

    public PromotionActionFactory(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
        promotionService = application.createPromotionService(elementFactory);
    }

    public PromotionsPage goToPromotions() {
        return promotionService.goToPromotions();
    }

    public Action<PromotionsDetailPage> goToDetails(final String title) {
        return new Action<PromotionsDetailPage>() {
            @Override
            public PromotionsDetailPage apply() {
                return promotionService.goToDetails(title);
            }
        };
    }

    public Action<PromotionsPage> makeDelete(final String title) {
        return new Action<PromotionsPage>() {
            @Override
            public PromotionsPage apply() {
                return promotionService.delete(title);
            }
        };
    }

    public Action<PromotionsPage> makeDeleteAll() {
        return new Action<PromotionsPage>() {
            @Override
            public PromotionsPage apply() {
                return promotionService.deleteAll();
            }
        };
    }

    public Action<SearchPage> makeCreateStaticPromotion(final StaticPromotion promotion) {
        return new Action<SearchPage>() {
            @Override
            public SearchPage apply() {
                return ((HSOPromotionService) promotionService).setUpStaticPromotion(promotion);
            }
        };
    }

    public Action<List<String>> makeCreatePromotion(final Promotion promotion, final Search search, final int numberOfDocs) {
        return new Action<List<String>>() {
            @Override
            public List<String> apply() {
                return promotionService.setUpPromotion(promotion, search, numberOfDocs);
            }
        };
    }
}
