package com.autonomy.abc.promotions;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.autonomy.abc.fixtures.PromotionTearDownStrategy;
import com.autonomy.abc.selenium.promotions.HsodPromotionService;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@Category(CoreFeature.class)
public class StaticPromotionsCoreITCase extends IsoHsodTestBase {
    private HsodPromotionService promotionService;
    private SearchService searchService;

    public StaticPromotionsCoreITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        promotionService = getApplication().promotionService();
        searchService = getApplication().searchService();
    }

    @After
    public void tearDown() {
        new PromotionTearDownStrategy().tearDown(this);
    }

    @Test
    public void testCreateStaticPromotion() {
        StaticPromotion promotion = new StaticPromotion("body", "content", "qwmbgh");
        promotionService.setUpStaticPromotion(promotion);
        SearchPage searchPage = searchService.search(promotion.getTrigger());
        assertThat(searchPage.getPromotedDocumentTitles(false), not(empty()));
    }
}
