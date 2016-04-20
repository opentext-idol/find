package com.autonomy.abc.fixtures;

import com.autonomy.abc.selenium.application.IsoApplication;

public class PromotionTearDownStrategy extends IsoTearDownStrategyBase {
    @Override
    protected void cleanUpApp(IsoApplication<?> app) {
        app.promotionService().deleteAll();
    }
}
