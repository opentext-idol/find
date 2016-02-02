package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.OPISOApplication;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.OPElementFactory;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.OPPromotionsPage;

public class OPPromotionService extends PromotionService<OPElementFactory> {
    public OPPromotionService(OPISOApplication application) {
        super(application);
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
