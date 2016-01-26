package com.autonomy.abc.promotions;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

@Category(CoreFeature.class)
public class StaticPromotionsCoreITCase extends HostedTestBase {
    private HSOPromotionService promotionService;
    private SearchService searchService;

    public StaticPromotionsCoreITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        promotionService = getApplication().createPromotionService(getElementFactory());
        searchService = getApplication().createSearchService(getElementFactory());
    }

    @After
    public void tearDown() {
        promotionService.deleteAll();
    }

    @Test
    public void testCreateStaticPromotion() {
        StaticPromotion promotion = new StaticPromotion("body", "content", "qwmbgh");
        promotionService.setUpStaticPromotion(promotion);
        SearchPage searchPage = searchService.search(promotion.getTrigger());
        assertThat(searchPage.getPromotedDocumentTitles(false), not(empty()));
    }
}
