package com.autonomy.abc.promotions;

import com.autonomy.abc.shared.SharedTriggerTests;
import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.SOTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.logging.RelatedTo;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.DriverUtil;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.state.TestStateAssert.assertThat;
import static com.autonomy.abc.framework.state.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ControlMatchers.url;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class CreateNewPromotionsITCase extends SOTestBase {

    public CreateNewPromotionsITCase(final TestConfig config) {
        super(config);
    }

    private SearchPage searchPage;
    private String promotedDocTitle;
    private PromotionsDetailPage promotionsDetailPage;
    private CreateNewPromotionsPage createPromotionsPage;
    private Wizard wizard;
    private SearchService searchService;
    private PromotionService promotionService;
    private TriggerForm triggerForm;

    private List<String> goToWizard(String query, int numberOfDocs) {
        searchPage = searchService.search(query);
        searchPage.openPromotionsBucket();
        List<String> promotedDocTitles = searchPage.addDocsToBucket(numberOfDocs);
        DriverUtil.waitUntilClickableThenClick(getDriver(), searchPage.promoteTheseItemsButton());
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        return promotedDocTitles;
    }

    private void finishPromotion() {
        createPromotionsPage.finishButton().click();
        searchPage = getElementFactory().getSearchPage();
    }

    private void goToTriggerStep() {
        Promotion promotion = new PinToPositionPromotion(2, "");
        wizard = promotion.makeWizard(createPromotionsPage);
        wizard.getCurrentStep().apply();
        wizard.next();
        wizard.getCurrentStep().apply();
        wizard.next();
        triggerForm = createPromotionsPage.getTriggerForm();
    }

    @Before
    public void setUp() {
        searchService = getApplication().searchService();
        promotionService = getApplication().promotionService();
        promotedDocTitle = goToWizard("fox", 1).get(0);
    }

    @After
    public void cleanUp() {
        SOTearDown.PROMOTIONS.tearDown(this);
    }

    @Test
    public void testPinToPositionSetPosition() {
        wizard = new PinToPositionPromotion(1, "whatever").makeWizard(createPromotionsPage);
        wizard.getCurrentStep().apply();
        wizard.next();

        createPromotionsPage.selectPositionPlusButton().click();
        verifyThat(createPromotionsPage.positionInputValue(), is(2));
        verifyThat(createPromotionsPage.continueButton(), not(hasAttribute(("disabled"))));
        verifyThat(createPromotionsPage.selectPositionMinusButton(), not(hasAttribute("disabled")));

        for (int i=0; i<4; i++) {
            createPromotionsPage.selectPositionPlusButton().click();
        }
        verifyThat(createPromotionsPage.positionInputValue(), is(6));

        for (int i=0; i<3; i++) {
            createPromotionsPage.selectPositionMinusButton().click();
        }
        verifyThat(createPromotionsPage.positionInputValue(), is(3));

        trySendKeysToPinPosition("16");
        verifyThat("pin position is not an input box", createPromotionsPage.positionInputValue(), is(3));

        wizard.cancel();
        verifyThat(getWindow(), url(not(containsString("create"))));
    }

    @Test
    public void testNonNumericEntryInPinToPosition() {
        createPromotionsPage.promotionType("PIN_TO_POSITION").click();
        createPromotionsPage.continueButton().click();
        Waits.loadOrFadeWait();
        Waits.loadOrFadeWait();
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition(Keys.CONTROL, "a");
        trySendKeysToPinPosition(Keys.CONTROL, "x");
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition("bad");
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition("1bad");
        getElementFactory().getSideNavBar().toggle();
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition("1");
        createPromotionsPage.selectPositionPlusButton().click();
        trySendKeysToPinPosition("bad");
        assertThat(createPromotionsPage.positionInputValue(), is(2));

        ElementUtil.tryClickThenTryParentClick(createPromotionsPage.continueButton());
        Waits.loadOrFadeWait();
        assertThat(createPromotionsPage, hasTextThat(containsString(SearchTriggerStep.TITLE)));
        getElementFactory().getSideNavBar().toggle();
    }

    private void trySendKeysToPinPosition(CharSequence... keys) {
        try {
            createPromotionsPage.pinToPositionInput().sendKeys(keys);
        } catch (final WebDriverException e) {
            // Chrome "cannot focus element" (this is good - we do not want to send keys)
        }
    }

    @Test
    public void testAddRemoveTriggerTermsAndCancel() {
        goToTriggerStep();

        assertThat(createPromotionsPage, containsText(wizard.getCurrentStep().getTitle()));

        SharedTriggerTests.addRemoveTriggers(triggerForm, createPromotionsPage.cancelButton(), createPromotionsPage.finishButton());

        createPromotionsPage.cancelButton().click();
        assertThat(getWindow(), url(not(containsString("create"))));
    }

    @Test
    public void testTriggers(){
        goToTriggerStep();
        SharedTriggerTests.badTriggersTest(triggerForm);
    }

    @Test
    public void testAddRemoveTriggersAndComplete() {
        goToTriggerStep();

        SharedTriggerTests.addRemoveTriggers(triggerForm, createPromotionsPage.cancelButton(), createPromotionsPage.finishButton());

        final String trigger = triggerForm.getTriggersAsStrings().get(0);
        finishPromotion();

        promotionsDetailPage = promotionService.goToDetails(trigger);

        verifyThat(promotionsDetailPage, containsText(trigger));
        verifyThat(promotionsDetailPage.pinPosition().getValue(), is("2"));
    }

    private void addSpotlightPromotion(final Promotion.SpotlightType spotlightType, final String searchTrigger) {
        Promotion promotion = new SpotlightPromotion(spotlightType, searchTrigger);

        wizard = promotion.makeWizard(createPromotionsPage);
        for (int i=0; i<wizard.getSteps().size(); i++) {
            verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
            WebElement button;
            if (wizard.onFinalStep()) {
                button = createPromotionsPage.finishButton();
            } else {
                button = createPromotionsPage.continueButton();
            }
            verifyThat(button, hasAttribute("disabled"));
            wizard.getCurrentStep().apply();
            verifyThat(button, not(hasAttribute("disabled")));
            wizard.next();
        }

        getElementFactory().getSearchPage();
        promotionsDetailPage = promotionService.goToDetails(searchTrigger);

        verifyThat(promotionsDetailPage, containsText("Spotlight for: " + searchTrigger));

        promotionsDetailPage.getTriggerForm().clickTrigger(searchTrigger);
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedDocTitle));
        if (isOnPrem()) {
            verifyThat(searchPage.getTopPromotedSpotlightType(), is(spotlightType.getOption()));
        }

        searchPage.modifiedResults().uncheck();
        Waits.loadOrFadeWait();
        verifyThat(searchPage, not(containsText(promotedDocTitle)));

        searchPage.modifiedResults().check();
        Waits.loadOrFadeWait();
        verifyThat(searchPage, containsText(promotedDocTitle));

        if (isOnPrem()) {
            promotionsDetailPage = promotionService.goToDetails(searchTrigger);
            verifyThat(promotionsDetailPage, containsText("Spotlight for: " + searchTrigger));
            verifyThat(promotionsDetailPage.spotlightTypeDropdown().getValue(), is(spotlightType.getOption()));
        }
    }

    @Test
    public void testAddSpotlightSponsored() {
        addSpotlightPromotion(Promotion.SpotlightType.SPONSORED, "apples");
    }

    @Test
    public void testAddSpotlightHotwire() {
        assumeThat(getConfig().getType(), is(ApplicationType.ON_PREM));
        addSpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "grapes");
    }

    @Test
    public void testAddSpotlightTopPromotions() {
        assumeThat(getConfig().getType(), is(ApplicationType.ON_PREM));
        addSpotlightPromotion(Promotion.SpotlightType.TOP_PROMOTIONS, "oranges");
    }

    @Test
    public void testAddPinToPosition() {
        final String searchTrigger = "animal";
        final int position = 2;
        wizard = new PinToPositionPromotion(position, searchTrigger).makeWizard(createPromotionsPage);

        for (int i=0; i<wizard.getSteps().size(); i++) {
            verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));

            WebElement button;
            if (wizard.onFinalStep()) {
                button = createPromotionsPage.finishButton();
            } else {
                button = createPromotionsPage.continueButton();
            }

            if (i == 1) {
                verifyThat(createPromotionsPage.positionInputValue(), is(1));
            } else {
                verifyThat(button, hasAttribute("disabled"));
            }
            wizard.getCurrentStep().apply();
            if (i == 1) {
                verifyThat(createPromotionsPage.positionInputValue(), is(2));
            }
            verifyThat(button, not(hasAttribute("disabled")));
            wizard.next();
        }

        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        promotionsDetailPage = promotionService.goToDetails("animal");

        verifyThat(promotionsDetailPage, containsText("animal"));
        verifyThat(promotionsDetailPage.pinPosition().getValue(), is("2"));

    }

    private void toggleAndCancel() {
        getElementFactory().getSideNavBar().toggle();
        createPromotionsPage.cancelButton().click();
        verifyThat(getWindow(), urlContains("search/modified"));
        verifyThat(searchPage.getBucketTitles(), hasSize(1));
        getElementFactory().getSideNavBar().toggle();
        DriverUtil.waitUntilClickableThenClick(getDriver(), searchPage.promoteTheseItemsButton());
        //        searchPage.promoteTheseItemsButton().click();
        createPromotionsPage.waitForLoad();
    }

    @Test
    public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
        verifyThat(getWindow(), url(endsWith("promotions/create")));
        toggleAndCancel();
        SpotlightPromotion spotlight = new SpotlightPromotion("whatever");
        wizard = spotlight.makeWizard(createPromotionsPage);
        wizard.getCurrentStep().apply();
        wizard.next();
        // TODO: refactor tests
        if (isOnPrem()) {
            verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
            toggleAndCancel();

            for (final Promotion.SpotlightType spotlightType : Promotion.SpotlightType.values()) {
                SpotlightPromotion promotion = new SpotlightPromotion(spotlightType, "whatever");
                wizard = promotion.makeWizard(createPromotionsPage);
                wizard.getCurrentStep().apply();
                wizard.next();
                wizard.getCurrentStep().apply();
                wizard.next();
                verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
                toggleAndCancel();
            }

            PinToPositionPromotion promotion = new PinToPositionPromotion(2, "whatever");
            wizard = promotion.makeWizard(createPromotionsPage);
            verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
            toggleAndCancel();

            wizard = promotion.makeWizard(createPromotionsPage);
            wizard.getCurrentStep().apply();
            wizard.next();
            wizard.getCurrentStep().apply();
            wizard.next();
        }
        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        toggleAndCancel();
    }

    @Test
    @RelatedTo("CSA-893")
    public void testNotificationsForPromotions() {
        final List<Promotion> promotions = new ArrayList<>();
        for (Promotion.SpotlightType type : Promotion.SpotlightType.values()) {
            String trigger = "MyFirstNotification" + type.getOption().replaceAll("\\s+", "");
            promotions.add(new SpotlightPromotion(type, trigger));
        }

        createPromotionsPage.cancelButton().click();
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.emptyBucket();
        searchPage.closePromotionsBucket();

        try {
            for (final Promotion promotion : promotions) {
                goToWizard("dog", 1);
                promotion.makeWizard(getElementFactory().getCreateNewPromotionsPage()).apply();

                new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationAppears());

                getElementFactory().getTopNavBar().notificationsDropdown();
                final NotificationsDropDown notifications = getElementFactory().getTopNavBar().getNotifications();
                //Match regardless of case
                verifyThat(notifications.notificationNumber(1).getText().toLowerCase(),
                        containsString(("Created a new spotlight promotion: Spotlight for: " + promotion.getTrigger()).toLowerCase()));

                // clicking notification should redirect to detail page?
//            notifications.notificationNumber(1).click();
//            verifyThat(getDriver().getCurrentUrl(), containsString("promotions/detail/spotlight"));
            }
        } finally {
            getElementFactory().getTopNavBar().closeNotifications();
        }
    }

    @Test
    public void testPromoteButtonInactiveWithEmptyBucketAfterPromotion() {
        goToTriggerStep();
        triggerForm.addTrigger("fox luke");
        finishPromotion();

        searchPage = getElementFactory().getSearchPage();
        searchPage.openPromotionsBucket();
        verifyThat(searchPage.getBucketTitles(), empty());
        verifyThat(searchPage.promoteTheseItemsButton(), hasAttribute("disabled"));
    }
}
