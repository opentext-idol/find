package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTearDown;
import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class CreateNewDynamicPromotionsHostedITCase extends HostedTestBase {
    private TriggerForm triggerForm;

    private SearchService searchService;

    public CreateNewDynamicPromotionsHostedITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        searchService = getApplication().searchService();
    }

    @After
    public void tearDown(){
        ABCTearDown.PROMOTIONS.tearDown(this);
    }

    // TODO: is there a nicer way to share this without
    // sharing the whole test?
    @Test
    public void testDynamicPromotionCreation() {
        SearchPage searchPage = searchService.search(new Query("bugs"));

        final String firstDocTitle = searchPage.getSearchResult(1).getTitleString();
        searchPage.promoteThisQueryButton().click();

        HSODCreateNewPromotionsPage dynamicPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        Waits.loadOrFadeWait();

        assertThat(getWindow(), urlContains("promotions/create-dynamic/"));
        assertThat(dynamicPromotionsPage.dial(), displayed());
        assertThat(dynamicPromotionsPage.getCurrentStepTitle(), containsString("Results number"));

        dynamicPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();
        triggerForm = dynamicPromotionsPage.getTriggerForm();

        assertThat(triggerForm.addButton(), displayed());
        assertThat(dynamicPromotionsPage.getCurrentStepTitle(), containsString("Trigger words"));
        assertThat(dynamicPromotionsPage.finishButton(), disabled());
        assertThat(triggerForm.addButton(), disabled());
        assertThat(triggerForm.getNumberOfTriggers(), is(0));

        checkAddTrigger("rabbit");
        checkAddTrigger("bunny");
        checkAddTrigger("hare");

        // Hare is not a word for bunny
        triggerForm.removeTrigger("hare");
        assertThat(triggerForm.getNumberOfTriggers(), is(2));
        assertThat(triggerForm.getTriggersAsStrings(), hasItems("bunny", "rabbit"));
        assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("hare")));

        dynamicPromotionsPage.finishButton().click();
        Waits.loadOrFadeWait();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();

        assertThat(searchPage.getHeadingSearchTerm(), is("bunny rabbit"));
        assertThat(searchPage.getPromotedDocumentTitles(false).get(0), is(firstDocTitle));
    }

    private void checkAddTrigger(String trigger) {
        List<String> beforeTriggers = triggerForm.getTriggersAsStrings();

        triggerForm.addTrigger(trigger);
        List<String> afterTriggers = triggerForm.getTriggersAsStrings();

        assertThat(afterTriggers, hasSize(beforeTriggers.size() + 1));
        assertThat(afterTriggers, hasItem(trigger));
        assertThat(afterTriggers, containsItems(beforeTriggers));
    }
}
