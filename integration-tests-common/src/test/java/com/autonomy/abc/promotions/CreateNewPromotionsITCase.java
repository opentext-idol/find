package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.autonomy.abc.selenium.util.Errors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class CreateNewPromotionsITCase extends ABCTestBase {

    public CreateNewPromotionsITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
        super(config, browser, appType, platform);
    }

    private SearchPage searchPage;
    private String promotedDocTitle;
    private PromotionsDetailPage promotionsDetailPage;
    private CreateNewPromotionsPage createPromotionsPage;
    private Wizard wizard;
    private SearchActionFactory actionFactory;
    private PromotionService promotionService;

    private List<String> goToWizard(Search search, int numberOfDocs) {
        search.apply();
        searchPage = getElementFactory().getSearchPage();
        searchPage.promoteTheseDocumentsButton().click();
        List<String> promotedDocTitles = searchPage.addToBucket(numberOfDocs);
        searchPage.waitUntilClickableThenClick(searchPage.promoteTheseItemsButton());
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
    }

    @Before
    public void setUp() {
        actionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        promotionService = getApplication().createPromotionService(getElementFactory());
        promotedDocTitle = goToWizard(actionFactory.makeSearch("fox"), 1).get(0);
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
    }

    @After
    public void cleanUp() {
        promotionService.deleteAll();
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
        verifyThat(getDriver().getCurrentUrl(), not(containsString("create")));
    }

    @Test
    public void testNonNumericEntryInPinToPosition() {
        createPromotionsPage.promotionType("PIN_TO_POSITION").click();
        createPromotionsPage.continueButton().click();
        createPromotionsPage.loadOrFadeWait();
        createPromotionsPage.loadOrFadeWait();
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition(Keys.CONTROL, "a");
        trySendKeysToPinPosition(Keys.CONTROL, "x");
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition("bad");
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition("1bad");
        body.getSideNavBar().toggle();
        assertThat(createPromotionsPage.positionInputValue(), is(1));

        trySendKeysToPinPosition("1");
        createPromotionsPage.selectPositionPlusButton().click();
        trySendKeysToPinPosition("bad");
        assertThat(createPromotionsPage.positionInputValue(), is(2));

        createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.continueButton());
        createPromotionsPage.loadOrFadeWait();
        assertThat(createPromotionsPage, hasTextThat(containsString(SearchTriggerStep.TITLE)));
        body.getSideNavBar().toggle();
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
        assertThat(createPromotionsPage.triggerAddButton(), disabled());
        assertThat(createPromotionsPage.cancelButton(), not(disabled()));

        createPromotionsPage.addSearchTrigger("animal");
        assertThat(createPromotionsPage.finishButton(), not(disabled()));
        assertThat(createPromotionsPage.getSearchTriggersList(), hasItem("animal"));

        createPromotionsPage.removeSearchTrigger("animal");
        assertThat(createPromotionsPage.getSearchTriggersList(), not(hasItem("animal")));
        assertThat(createPromotionsPage.finishButton(), disabled());

        createPromotionsPage.addSearchTrigger("bushy tail");
        assertThat(createPromotionsPage.getSearchTriggersList(), hasSize(2));
        assertThat(createPromotionsPage.getSearchTriggersList(), hasItem("bushy"));
        assertThat(createPromotionsPage.getSearchTriggersList(), hasItem("tail"));

        createPromotionsPage.removeSearchTrigger("tail");
        assertThat(createPromotionsPage.getSearchTriggersList(), hasSize(1));
        assertThat(createPromotionsPage.getSearchTriggersList(), hasItem("bushy"));
        assertThat(createPromotionsPage.getSearchTriggersList(), not(hasItem("tail")));

        createPromotionsPage.cancelButton().click();
        assertThat(getDriver().getCurrentUrl(), not(containsString("create")));
    }

    @Test
    public void testWhitespaceTrigger() {
        goToTriggerStep();
        assertThat(createPromotionsPage.triggerAddButton(), disabled());

        createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.triggerAddButton());
        assertThat(createPromotionsPage.getSearchTriggersList(), empty());

        createPromotionsPage.addSearchTrigger("trigger");
        assertThat(createPromotionsPage.getSearchTriggersList(), hasSize(1));

        String[] invalidTriggers = {"   ", " trigger", "\t"};
        for (String trigger : invalidTriggers) {
            createPromotionsPage.addSearchTrigger(trigger);
            verifyThat("'" + trigger + "' is not accepted as a valid trigger", createPromotionsPage.getSearchTriggersList(), hasSize(1));
        }
    }

    @Test
    public void testQuotesTrigger() {
        goToTriggerStep();
        assertThat(createPromotionsPage.triggerAddButton(), disabled());

        createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.triggerAddButton());
        assertThat(createPromotionsPage.getSearchTriggersList(), empty());

        createPromotionsPage.addSearchTrigger("bag");
        assertThat(createPromotionsPage.getSearchTriggersList(), hasSize(1));

        String[] invalidTriggers = {"\"bag", "bag\"", "\"bag\""};
        for (String trigger : invalidTriggers) {
            createPromotionsPage.addSearchTrigger(trigger);
            assertThat("'" + trigger + "' is not accepted as a valid trigger", createPromotionsPage.getSearchTriggersList(), hasSize(1));
        }

        createPromotionsPage.removeSearchTrigger("bag");
        assertThat(createPromotionsPage.getSearchTriggersList(), empty());
    }

    @Test
    public void testCommasTrigger() {
        goToTriggerStep();
        createPromotionsPage.addSearchTrigger("France");
        assertThat(createPromotionsPage.getSearchTriggersList(), hasSize(1));

        String[] invalidTriggers = {",Germany", "Ita,ly Spain", "Ireland, Belgium", "UK , Luxembourg"};
        for (String trigger : invalidTriggers) {
            createPromotionsPage.addSearchTrigger(trigger);
            assertThat("'" + trigger + "' is not accepted as a valid trigger", createPromotionsPage.getSearchTriggersList(), hasSize(1));
            assertThat(createPromotionsPage, containsText(Errors.Term.COMMAS));
        }

        createPromotionsPage.addSearchTrigger("Andorra");
        assertThat(createPromotionsPage.getSearchTriggersList(), hasSize(2));
        assertThat(createPromotionsPage, not(containsText(Errors.Term.COMMAS)));
    }

    @Test
    public void testHTMLTrigger() {
        goToTriggerStep();
        final String searchTrigger = "<h1>hey</h1>";
        createPromotionsPage.addSearchTrigger(searchTrigger);

        final WebElement span = createPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
        assertThat("HTML was escaped", span, hasTextThat(equalTo(searchTrigger)));
    }

    @Test
    public void testAddRemoveTriggersAndComplete() {
        goToTriggerStep();
        createPromotionsPage.addSearchTrigger("alpha");
        createPromotionsPage.addSearchTrigger("beta gamma delta");
        createPromotionsPage.removeSearchTrigger("gamma");
        createPromotionsPage.removeSearchTrigger("alpha");
        createPromotionsPage.addSearchTrigger("epsilon");
        createPromotionsPage.removeSearchTrigger("beta");
        verifyThat(createPromotionsPage.getSearchTriggersList(), hasSize(2));

        finishPromotion();

        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        promotionsDetailPage = promotionService.goToDetails("delta");

        verifyThat(promotionsDetailPage, containsText("delta"));
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

        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        promotionsDetailPage = promotionService.goToDetails(searchTrigger);

        verifyThat(promotionsDetailPage, containsText("Spotlight for: " + searchTrigger));

        promotionsDetailPage.trigger(searchTrigger).click();
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedDocTitle));
        if (config.getType().equals(ApplicationType.ON_PREM)) {
            verifyThat(searchPage.getTopPromotedSpotlightType(), is(spotlightType.getOption()));
        }

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, not(containsText(promotedDocTitle)));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, containsText(promotedDocTitle));

        if (config.getType().equals(ApplicationType.ON_PREM)) {
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
        assumeThat(config.getType(), is(ApplicationType.ON_PREM));
        addSpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "grapes");
    }

    @Test
    public void testAddSpotlightTopPromotions() {
        assumeThat(config.getType(), is(ApplicationType.ON_PREM));
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
        body.getSideNavBar().toggle();
        createPromotionsPage.cancelButton().click();
        verifyThat(getDriver().getCurrentUrl(), containsString("search/modified"));
        verifyThat(searchPage.promotedItemsCount(), is(1));
        body.getSideNavBar().toggle();
        searchPage.waitUntilClickableThenClick(searchPage.promoteTheseItemsButton());
//        searchPage.promoteTheseItemsButton().click();
        createPromotionsPage.waitForLoad();
    }

    @Test
    public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
        verifyThat(getDriver().getCurrentUrl(), endsWith("promotions/create"));
        toggleAndCancel();
        SpotlightPromotion spotlight = new SpotlightPromotion("whatever");
        wizard = spotlight.makeWizard(createPromotionsPage);
        wizard.getCurrentStep().apply();
        wizard.next();
        // TODO: refactor tests
        if (config.getType() == ApplicationType.ON_PREM) {
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
    public void testNotificationsForPromotions() {
        createPromotionsPage.cancelButton().click();
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.searchResultCheckbox(1).click();
        searchPage.promotionsBucketClose();

        for (final String spotlightType : Arrays.asList("Sponsored", "Hotwire", "Top Promotions")) {
            actionFactory.makeSearch("dog").apply();
            searchPage = getElementFactory().getSearchPage();
            searchPage.createAPromotion();

            createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
            createPromotionsPage.addSpotlightPromotion(spotlightType, "MyFirstNotification" + spotlightType.replaceAll("\\s+", ""));
            new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
//            searchPage.waitForGritterToClear();

            body.getTopNavBar().notificationsDropdown();
            final NotificationsDropDown notifications = body.getTopNavBar().getNotifications();
//            body.getTopNavBar().loadOrFadeWait();
            //Match regardless of case
            verifyThat(notifications.notificationNumber(1).getText().toLowerCase(),
                    containsString(("Created a new spotlight promotion: Spotlight for: MyFirstNotification" + spotlightType.replaceAll("\\s+", "")).toLowerCase()));

            // TODO: CSA-893
//            notifications.notificationNumber(1).click();
//            verifyThat(getDriver().getCurrentUrl(), containsString("promotions/detail/spotlight"));
        }
    }

    @Test
    public void testPromoteButtonInactiveWithEmptyBucketAfterPromotion() {
        goToTriggerStep();
        createPromotionsPage.addSearchTrigger("fox luke");
        finishPromotion();
        createPromotionsPage.loadOrFadeWait();

        new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        searchPage.promoteTheseDocumentsButton().click();
        verifyThat(searchPage.promotionsBucketWebElements(), hasSize(0));
        verifyThat(searchPage.promoteTheseItemsButton(), hasAttribute("disabled"));
    }
}
