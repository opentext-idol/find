package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.autonomy.abc.selenium.search.SearchPage;

public class HSODPromotionService extends PromotionService<IsoHsodElementFactory> {
    public HSODPromotionService(SearchOptimizerApplication<? extends IsoHsodElementFactory> application) {
        super(application);
    }

    @Override
    public HSODPromotionsPage goToPromotions() {
        return (HSODPromotionsPage) super.goToPromotions();
    }

    public SearchPage setUpStaticPromotion(StaticPromotion promotion) {
        HSODPromotionsPage promotionsPage = goToPromotions();
        promotionsPage.staticPromotionButton().click();
        HSODCreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotion.makeWizard(createNewPromotionsPage).apply();
        return getElementFactory().getSearchPage();
    }
}
