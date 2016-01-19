package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.OPElementFactory;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;

public class OPPromotionService extends PromotionService {
    private OPElementFactory elementFactory;

    public OPPromotionService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
        this.elementFactory = (OPElementFactory) elementFactory;
    }

    @Override
    protected OPElementFactory getElementFactory() {
        return elementFactory;
    }

    // TODO: is there a better way to do this? generics?
    @Override
    public OPPromotionsPage goToPromotions() {
        return (OPPromotionsPage) super.goToPromotions();
    }

    @Override
    public OPPromotionsPage deleteAll() {
        return (OPPromotionsPage) super.deleteAll();
    }

    @Override
    public OPPromotionsDetailPage goToDetails(String title) {
        return (OPPromotionsDetailPage) super.goToDetails(title);
    }

    @Override
    public OPPromotionsDetailPage goToDetails(Promotion promotion) {
        return (OPPromotionsDetailPage) super.goToDetails(promotion);
    }
}
