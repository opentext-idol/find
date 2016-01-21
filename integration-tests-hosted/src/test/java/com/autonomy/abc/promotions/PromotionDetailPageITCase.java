package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.PromotionsDetailTriggerForm;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class PromotionDetailPageITCase extends ABCTestBase {

    private SearchService searchService;
    private PromotionService<?> promotionService;
    private PromotionsDetailPage promotionsDetailPage;


    public PromotionDetailPageITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        searchService = getApplication().createSearchService(getElementFactory());
        promotionService = getApplication().createPromotionService(getElementFactory());
    }

    @Test
    @KnownBug("CCUK-3586")
    public void testEditDynamicQuery() throws InterruptedException {
        final String initialTrigger = "meow";
        final String updateTrigger = "tigre";
        final String initialQueryTerm = "chat";
        final String updateQueryTerm = "kitty";

        SearchPage searchPage = searchService.search(new SearchQuery(updateQueryTerm).withFilter(new LanguageFilter(Language.FRENCH)));
        final String updatePromotedResult = searchPage.searchResult(1).getText();
        Promotion promotion = new DynamicPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, initialTrigger);
        final String initialPromotedResult = promotionService.setUpPromotion(promotion, new SearchQuery(initialQueryTerm).withFilter(new LanguageFilter(Language.FRENCH)), 1).get(0);

        promotionsDetailPage = promotionService.goToDetails(promotion);
        PromotionsDetailTriggerForm triggerForm = promotionsDetailPage.getTriggerForm();
        triggerForm.addTrigger(updateTrigger);
        triggerForm.removeTrigger(initialTrigger);

        searchService.search(new SearchQuery(updateTrigger).withFilter(new LanguageFilter(Language.FRENCH)));
        verifyThat(searchPage.getPromotedDocumentTitles(false).get(0), is(initialPromotedResult));

        promotionsDetailPage = promotionService.goToDetails(initialTrigger);

        Editable queryText = promotionsDetailPage.queryText();
        verifyThat("correct query text displayed", queryText.getValue(), is(initialQueryTerm));

        queryText.setValueAndWait(updateQueryTerm);
        verifyThat("query text updated", queryText.getValue(), is(updateQueryTerm));

        searchService.search(new SearchQuery(updateTrigger).withFilter(new LanguageFilter(Language.FRENCH)));
        verifyThat("promoted query updated in search results", searchPage.getPromotedDocumentTitles(false).get(0), is(updatePromotedResult));

        getDriver().navigate().refresh();
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        verifyThat("correct promoted result after page refresh", searchPage.getPromotedDocumentTitles(false).get(0), is(updatePromotedResult));
    }

    @Test
    @KnownBug("CSA-1494")
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
    @KnownBug("CSA-1769")
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
