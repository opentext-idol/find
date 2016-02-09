package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;

public class HSODPromotionService extends PromotionService<HSODElementFactory> {
    public HSODPromotionService(SearchOptimizerApplication<? extends HSODElementFactory> application) {
        super(application);
    }

    @Override
    public HSOPromotionsPage goToPromotions() {
        return (HSOPromotionsPage) super.goToPromotions();
    }

    public SearchPage setUpStaticPromotion(StaticPromotion promotion) {
        HSOPromotionsPage promotionsPage = goToPromotions();
        promotionsPage.staticPromotionButton().click();
        HSOCreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotion.makeWizard(createNewPromotionsPage).apply();
        return getElementFactory().getSearchPage();
    }
}
