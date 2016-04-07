package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;

public class IdolPromotionService extends PromotionService<IdolIsoElementFactory> {
    public IdolPromotionService(SearchOptimizerApplication<? extends IdolIsoElementFactory> application) {
        super(application);
    }

    // TODO: is there a better way to do this? generics?
    @Override
    public IdolPromotionsPage goToPromotions() {
        return (IdolPromotionsPage) super.goToPromotions();
    }

    @Override
    public IdolPromotionsPage deleteAll() {
        return (IdolPromotionsPage) super.deleteAll();
    }

    @Override
    public IdolPromotionsDetailPage goToDetails(String title) {
        return (IdolPromotionsDetailPage) super.goToDetails(title);
    }

    @Override
    public IdolPromotionsDetailPage goToDetails(Promotion promotion) {
        return (IdolPromotionsDetailPage) super.goToDetails(promotion);
    }
}
