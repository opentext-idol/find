package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.iso.OPISOApplication;
import com.autonomy.abc.selenium.iso.OPISOElementFactory;

public class OPPromotionService extends PromotionService<OPISOElementFactory> {
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
