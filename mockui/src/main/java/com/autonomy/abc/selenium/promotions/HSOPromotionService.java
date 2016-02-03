package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.HSOApplication;
import com.autonomy.abc.selenium.page.HSODElementFactory;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;

public class HSOPromotionService extends PromotionService<HSODElementFactory> {
    private HSOPromotionsPage promotionsPage;
    private HSOCreateNewPromotionsPage createNewPromotionsPage;

    public HSOPromotionService(HSOApplication application) {
        super(application);
    }

    @Override
    public HSOPromotionsPage goToPromotions() {
        return (HSOPromotionsPage) super.goToPromotions();
    }

    public SearchPage setUpStaticPromotion(StaticPromotion promotion) {
        promotionsPage = goToPromotions();
        promotionsPage.staticPromotionButton().click();
        createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotion.makeWizard(createNewPromotionsPage).apply();
        return getElementFactory().getSearchPage();
    }
}
