package com.autonomy.abc.promotions;

import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.promotions.HSODPromotionService;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@Category(CoreFeature.class)
public class StaticPromotionsCoreITCase extends HostedTestBase {
    private HSODPromotionService promotionService;
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
        SOTearDown.PROMOTIONS.tearDown(this);
    }

    @Test
    public void testCreateStaticPromotion() {
        StaticPromotion promotion = new StaticPromotion("body", "content", "qwmbgh");
        promotionService.setUpStaticPromotion(promotion);
        SearchPage searchPage = searchService.search(promotion.getTrigger());
        assertThat(searchPage.getPromotedDocumentTitles(false), not(empty()));
    }
}
