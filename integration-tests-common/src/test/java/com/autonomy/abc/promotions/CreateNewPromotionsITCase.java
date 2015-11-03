package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
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
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class CreateNewPromotionsITCase extends ABCTestBase {

    public CreateNewPromotionsITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
        super(config, browser, appType, platform);
    }

    private SearchPage searchPage;
    private String promotedDocTitle;
    private PromotionsPage promotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private CreateNewPromotionsPage createPromotionsPage;
    private Wizard wizard;
    private SearchActionFactory actionFactory;
    private PromotionActionFactory promotionActionFactory;

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

    @Before
    public void setUp() {
        actionFactory = new SearchActionFactory(getApplication(), getElementFactory());
        promotionActionFactory = new PromotionActionFactory(getApplication(), getElementFactory());
        promotedDocTitle = goToWizard(actionFactory.makeSearch("fox"), 1).get(0);
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
    }

    @After
    public void cleanUp() {
        promotionActionFactory.makeDeleteAll().apply();
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

        try {
            // Try to send keys in case this gets changed back to an input
            createPromotionsPage.pinToPositionInput().sendKeys("16");
            verifyThat(createPromotionsPage.positionInputValue(), is(3));
        } catch (final WebDriverException e) {
            //try catch because chrome struggles to focus on this element
        }

        wizard.cancel();
        verifyThat(getDriver().getCurrentUrl(), not(containsString("create")));
    }

    @Test
    public void testAddRemoveTriggerTermsAndCancel() {
        createPromotionsPage.navigateToTriggers();
        assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
        assertThat("Trigger add button is not disabled when text box is empty", createPromotionsPage.isAttributePresent(createPromotionsPage.triggerAddButton(), "disabled"));
        assertThat("Trigger add button is not disabled when text box is empty", !createPromotionsPage.isAttributePresent(createPromotionsPage.cancelButton(), "disabled"));

        createPromotionsPage.addSearchTrigger("animal");
        assertThat("Promote button is not enabled when a trigger is added", !createPromotionsPage.isAttributePresent(createPromotionsPage.finishButton(), "disabled"));
        assertThat("animal search trigger not added", createPromotionsPage.getSearchTriggersList().contains("animal"));

        createPromotionsPage.removeSearchTrigger("animal");
        assertThat("animal search trigger not removed", !createPromotionsPage.getSearchTriggersList().contains("animal"));
        assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.isAttributePresent(createPromotionsPage.finishButton(), "disabled"));

        createPromotionsPage.addSearchTrigger("bushy tail");
        assertThat("Number of triggers does not equal 2", createPromotionsPage.getSearchTriggersList().size() == 2);
        assertThat("bushy search trigger not added", createPromotionsPage.getSearchTriggersList().contains("bushy"));
        assertThat("tail search trigger not added", createPromotionsPage.getSearchTriggersList().contains("tail"));

        createPromotionsPage.removeSearchTrigger("tail");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);
        assertThat("bushy search trigger not present", createPromotionsPage.getSearchTriggersList().contains("bushy"));
        assertThat("tail search trigger not removed", !createPromotionsPage.getSearchTriggersList().contains("tail"));

        createPromotionsPage.cancelButton().click();
        assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
    }

    @Test
    public void testWhitespaceTrigger() {
        createPromotionsPage.navigateToTriggers();
        assertThat("Trigger add button is not disabled", createPromotionsPage.isAttributePresent(createPromotionsPage.triggerAddButton(), "disabled"));

        createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.triggerAddButton());
        assertThat("Number of triggers does not equal 0", createPromotionsPage.getSearchTriggersList().size() == 0);

        createPromotionsPage.addSearchTrigger("trigger");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger("   ");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger(" trigger");
        assertThat("Whitespace at beginning should be ignored", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger("\t");
        assertThat("Whitespace at beginning should be ignored", createPromotionsPage.getSearchTriggersList().size() == 1);
    }

    @Test
    public void testQuotesTrigger() {
        createPromotionsPage.navigateToTriggers();
        assertThat("Trigger add button is not disabled", createPromotionsPage.isAttributePresent(createPromotionsPage.triggerAddButton(), "disabled"));

        createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.triggerAddButton());

        assertThat("Number of triggers does not equal 0", createPromotionsPage.getSearchTriggersList().size() == 0);

        createPromotionsPage.addSearchTrigger("bag");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger("\"bag");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger("bag\"");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger("\"bag\"");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.removeSearchTrigger("bag");
        assertThat("Number of triggers does not equal 0", createPromotionsPage.getSearchTriggersList().size() == 0);
    }

    @Test
    public void testCommasTrigger() {
        createPromotionsPage.navigateToTriggers();
        createPromotionsPage.addSearchTrigger("France");
        assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

        createPromotionsPage.addSearchTrigger(",Germany");
        assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 1);
        assertThat("Incorrect/No error message displayed", createPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        createPromotionsPage.addSearchTrigger("Ita,ly Spain");
        assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 1);
        assertThat("Incorrect/No error message displayed", createPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        createPromotionsPage.addSearchTrigger("Ireland, Belgium");
        assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 1);
        assertThat("Incorrect/No error message displayed", createPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        createPromotionsPage.addSearchTrigger("UK , Luxembourg");
        assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 1);
        assertThat("Incorrect/No error message displayed", createPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

        createPromotionsPage.addSearchTrigger("Andorra");
        assertThat("Legitimate trigger not added", createPromotionsPage.getSearchTriggersList().size() == 2);
        assertThat("Error message displayed with legitimate term", !createPromotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));
    }

    @Test
    public void testHTMLTrigger() {
        createPromotionsPage.navigateToTriggers();
        final String searchTrigger = "<h1>hey</h1>";
        createPromotionsPage.addSearchTrigger(searchTrigger);

        final WebElement span = createPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
        assertThat("HTML was not escaped", span.getText().equals(searchTrigger));
    }

    @Test
    public void testNonNumericEntryInPinToPosition() {
        createPromotionsPage.promotionType("PIN_TO_POSITION").click();
        createPromotionsPage.continueButton().click();
        createPromotionsPage.loadOrFadeWait();
        createPromotionsPage.loadOrFadeWait();
        assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

        try {
            createPromotionsPage.pinToPositionInput().sendKeys(Keys.CONTROL, "a");
            createPromotionsPage.pinToPositionInput().sendKeys(Keys.CONTROL, "x");
            assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

            createPromotionsPage.pinToPositionInput().sendKeys("bad");
            assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

            createPromotionsPage.pinToPositionInput().sendKeys("1bad");
            body.getTopNavBar().sideBarToggle();
            assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

            createPromotionsPage.pinToPositionInput().sendKeys("1");
            createPromotionsPage.selectPositionPlusButton().click();
            createPromotionsPage.pinToPositionInput().sendKeys("bad");
            assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 2);

            createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.continueButton());
            createPromotionsPage.loadOrFadeWait();
            assertThat("Wizard has not progressed with a legitimate position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
        } catch (final WebDriverException e) {
            //try catch because Chrome struggles to focus on pinToPositionInput
        }
    }

    @Test
    public void testAddRemoveTriggersAndComplete() {
        createPromotionsPage.navigateToTriggers();
        createPromotionsPage.addSearchTrigger("alpha");
        createPromotionsPage.addSearchTrigger("beta gamma delta");
        createPromotionsPage.removeSearchTrigger("gamma");
        createPromotionsPage.removeSearchTrigger("alpha");
        createPromotionsPage.addSearchTrigger("epsilon");
        createPromotionsPage.removeSearchTrigger("beta");
        verifyThat(createPromotionsPage.getSearchTriggersList(), hasSize(2));

        finishPromotion();

        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        promotionActionFactory.goToDetails("delta").apply();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();

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
        promotionActionFactory.goToDetails(searchTrigger).apply();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();

        verifyThat(promotionsDetailPage, containsText("Spotlight for: " + searchTrigger));

        promotionsDetailPage.trigger(searchTrigger).click();
        searchPage = getElementFactory().getSearchPage();

        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedDocTitle));
        verifyThat(searchPage.getTopPromotedLinkButtonText(), is(spotlightType.getOption()));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, not(containsText(promotedDocTitle)));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, containsText(promotedDocTitle));

        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions"));
        promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

        verifyThat(promotionsPage, containsText("Spotlight for: " + searchTrigger));
        verifyThat(promotionsDetailPage.spotlightTypeDropdown().getValue(), is(spotlightType.getOption()));
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
        promotionActionFactory.goToDetails("animal").apply();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();

        verifyThat(promotionsDetailPage, containsText("animal"));
//        verifyThat(promotionsPage.promotionPosition(), containsText("2"));

    }

    private void toggleAndCancel() {
        body.getTopNavBar().sideBarToggle();
        createPromotionsPage.cancelButton().click();
        verifyThat(getDriver().getCurrentUrl(), containsString("search/modified"));
        verifyThat(searchPage.promotedItemsCount(), is(1));
        body.getTopNavBar().sideBarToggle();
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
        createPromotionsPage.navigateToTriggers();
        createPromotionsPage.addSearchTrigger("fox luke");
        finishPromotion();
        createPromotionsPage.loadOrFadeWait();

        new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        searchPage.promoteTheseDocumentsButton().click();
        verifyThat(searchPage.promotionsBucketWebElements(), hasSize(0));
        verifyThat(searchPage.promoteTheseItemsButton(), hasAttribute("disabled"));
    }
}
