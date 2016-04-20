package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.autonomy.abc.selenium.search.SearchPage;

public class HsodPromotionService extends PromotionService<IsoHsodElementFactory> {
    public HsodPromotionService(IsoApplication<? extends IsoHsodElementFactory> application) {
        super(application);
    }

    @Override
    public HsodPromotionsPage goToPromotions() {
        return (HsodPromotionsPage) super.goToPromotions();
    }

    public SearchPage setUpStaticPromotion(StaticPromotion promotion) {
        HsodPromotionsPage promotionsPage = goToPromotions();
        promotionsPage.staticPromotionButton().click();
        HsodCreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotion.makeWizard(createNewPromotionsPage).apply();
        return getElementFactory().getSearchPage();
    }
}
