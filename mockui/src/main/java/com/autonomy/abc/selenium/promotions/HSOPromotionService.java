package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.config.SearchOptimizerApplication;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;

public class HSOPromotionService extends PromotionService {
    private HSOPromotionsPage promotionsPage;
    private HSOCreateNewPromotionsPage createNewPromotionsPage;
    private HSOElementFactory elementFactory;

    public HSOPromotionService(SearchOptimizerApplication application, ElementFactory elementFactory) {
        super(application, elementFactory);
        this.elementFactory = (HSOElementFactory) elementFactory;
    }

    @Override
    protected HSOElementFactory getElementFactory() {
        return elementFactory;
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
