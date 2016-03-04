package com.autonomy.abc.config;

import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.promotions.PromotionService;

public enum ABCTearDown implements TearDown<ABCTestBase> {
    KEYWORDS {
        @Override
        public void tearDown(ABCTestBase test) {
            KeywordService service = test.getApplication().keywordService();
            if (service != null) {
                service.deleteAll(KeywordFilter.ALL);
            }
        }
    },
    PROMOTIONS {
        @Override
        public void tearDown(ABCTestBase test) {
            PromotionService<?> service = test.getApplication().promotionService();
            if (service != null) {
                service.deleteAll();
            }
        }
    };

    @Override
    public abstract void tearDown(ABCTestBase test);
}
