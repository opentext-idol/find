package com.autonomy.abc.promotions;

import com.autonomy.abc.config.SOTearDown;
import com.autonomy.abc.config.SOTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

@Category(CoreFeature.class)
public class PromotionsCoreITCase extends SOTestBase {
    private SearchPage searchPage;

    private PromotionService<?> promotionService;
    private SearchService searchService;

    public PromotionsCoreITCase(TestConfig config) {
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
    public void testCreateSpotlight() {
        create(new SpotlightPromotion("yellow"));
        assertThat(searchPage.getPromotedDocumentTitles(false), not(empty()));
    }

    @Test
    public void testCreatePinToPosition() {
        create(new PinToPositionPromotion(1, "zxplmn"));
        assertThat(searchPage.visibleDocumentsCount(), greaterThan(0));
    }

    @Test
    public void testCreateDynamicPromotion() {
        create(new DynamicPromotion(5, "auto"));
        assertThat(searchPage.getPromotedDocumentTitles(false), not(empty()));
    }

    private void create(Promotion promotion) {
        promotionService.setUpPromotion(promotion, new Query("car"), 1);
        searchPage = searchService.search(promotion.getTrigger());
    }

    @Test
    public void testDeletePromotion() {
        Promotion promotion = new SpotlightPromotion("qwlmdbsk");
        create(promotion);
        promotionService.delete(promotion);
        assertThat(promotionService.goToPromotions(), not(containsText(promotion.getTrigger())));
    }

    @Test
    public void testViewDetails() {
        Promotion promotion = new SpotlightPromotion("vegetables");
        create(promotion);
        promotionService.goToDetails(promotion);
        assertThat(getElementFactory().getPromotionsDetailPage(), containsText(promotion.getTrigger()));
    }

}
