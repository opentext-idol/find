package com.autonomy.abc.promotions;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.element.PromotionsDetailTriggerForm;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.Editable;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;

public class PromotionDetailPageITCase extends HybridIsoTestBase {

    private SearchService searchService;
    private PromotionService<?> promotionService;
    private PromotionsDetailPage promotionsDetailPage;


    public PromotionDetailPageITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        searchService = getApplication().searchService();
        promotionService = getApplication().promotionService();
    }

    @Test
    @ResolvedBug("CCUK-3586")
    public void testEditDynamicQuery() throws InterruptedException {
        final String initialTrigger = "meow";
        final String updateTrigger = "tigre";
        final String initialQueryTerm = "chat";
        final String updateQueryTerm = "kitty";

        SearchPage searchPage = searchService.search(new Query(updateQueryTerm).withFilter(new LanguageFilter(Language.FRENCH)));
        final String updatePromotedResult = searchPage.getSearchResult(1).title().getText();
        Promotion promotion = new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, initialTrigger);
        final String initialPromotedResult = promotionService.setUpPromotion(promotion, new Query(initialQueryTerm).withFilter(new LanguageFilter(Language.FRENCH)), 1).get(0);

        promotionsDetailPage = promotionService.goToDetails(promotion);
        PromotionsDetailTriggerForm triggerForm = promotionsDetailPage.getTriggerForm();
        triggerForm.addTrigger(updateTrigger);
        triggerForm.removeTrigger(initialTrigger);

        searchService.search(new Query(updateTrigger).withFilter(new LanguageFilter(Language.FRENCH)));
        verifyThat(searchPage.getPromotedDocumentTitles(false).get(0), is(initialPromotedResult));

        promotionsDetailPage = promotionService.goToDetails(initialTrigger);

        Editable queryText = promotionsDetailPage.queryText();
        verifyThat("correct query text displayed", queryText.getValue(), is(initialQueryTerm));

        queryText.setValueAndWait(updateQueryTerm);
        verifyThat("query text updated", queryText.getValue(), is(updateQueryTerm));

        searchService.search(new Query(updateTrigger).withFilter(new LanguageFilter(Language.FRENCH)));
        verifyThat("promoted query updated in search results", searchPage.getPromotedDocumentTitles(false).get(0), is(updatePromotedResult));

        getWindow().refresh();
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        verifyThat("correct promoted result after page refresh", searchPage.getPromotedDocumentTitles(false).get(0), is(updatePromotedResult));
    }

    @Test
    @ResolvedBug("CSA-1494")
    public void testAddingMultipleTriggersNotifications() {
        Promotion promotion = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE,"moscow");

        promotionService.setUpPromotion(promotion, "Mother Russia", 4);
        promotionsDetailPage = promotionService.goToDetails(promotion);

        String[] triggers = {"HC", "Sochi", "CKSA", "SKA", "Dinamo", "Riga"};
        promotionsDetailPage.getTriggerForm().addTrigger(StringUtils.join(triggers, ' '));

        getElementFactory().getTopNavBar().notificationsDropdown();

        verifyThat(getElementFactory().getTopNavBar().getNotifications().getAllNotificationMessages(), hasItem("Edited a spotlight promotion"));

        for(String notification : getElementFactory().getTopNavBar().getNotifications().getAllNotificationMessages()){
            for(String trigger : triggers){
                verifyThat(notification, not(containsString(trigger)));
            }
        }
    }


    @Test
    @ResolvedBug("CSA-1769")
    public void testUpdatingAndDeletingPinToPosition(){
        PinToPositionPromotion pinToPositionPromotion = new PinToPositionPromotion(1, "say anything");

        promotionService.setUpPromotion(pinToPositionPromotion, "Max Bemis", 2);
        promotionsDetailPage = promotionService.goToDetails(pinToPositionPromotion);

        promotionsDetailPage.pinPosition().setValueAndWait("4");
        verifyThat(promotionsDetailPage.pinPosition().getValue(), is("4"));

        String newTitle = "Admit It!!!";

        promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
        Waits.loadOrFadeWait();
        verifyThat(promotionsDetailPage.promotionTitle().getValue(), is(newTitle));

        promotionService.delete(newTitle);
        verifyThat(getElementFactory().getPromotionsPage().getPromotionTitles(), not(hasItem(newTitle)));
    }
}
