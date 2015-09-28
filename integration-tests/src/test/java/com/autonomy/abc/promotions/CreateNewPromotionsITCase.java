package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.Wizard;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
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
        // TODO: fix magic sleep
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        promotedDocTitle = goToWizard(actionFactory.makeSearch("fox"), 1).get(0);
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        wizard = new Wizard(createPromotionsPage);
    }

    @After
    public void cleanUp() {
        promotionActionFactory.makeDeleteAll().apply();
    }

    @Test
    public void testAddPinToPosition() {
        verifyThat(createPromotionsPage.getCurrentStepTitle(), containsString("Promotion type"));
        createPromotionsPage.promotionType("PIN_TO_POSITION").click();
        createPromotionsPage.continueButton(CreateNewPromotionsBase.WizardStep.TYPE).click();
        createPromotionsPage.loadOrFadeWait();
        verifyThat(createPromotionsPage.getCurrentStepTitle(), containsString("Promotion details"));
        verifyThat(createPromotionsPage.positionInputValue(), equalTo(1));
        verifyThat(createPromotionsPage.selectPositionMinusButton(), hasAttribute("disabled"));

        createPromotionsPage.waitUntilClickableThenClick(createPromotionsPage.selectPositionPlusButton());
        createPromotionsPage.loadOrFadeWait();
        verifyThat(createPromotionsPage.positionInputValue(), equalTo(2));
        verifyThat(createPromotionsPage.selectPositionMinusButton(), not(hasAttribute("disabled")));

        createPromotionsPage.continueButton(CreateNewPromotionsBase.WizardStep.PROMOTION_TYPE).click();
        createPromotionsPage.loadOrFadeWait();
        verifyThat(createPromotionsPage.getCurrentStepTitle(), containsString("Promotion triggers"));
        verifyThat(createPromotionsPage, containsText("Select Promotion Triggers"));
        verifyThat(createPromotionsPage.finishButton(), hasAttribute("disabled"));

        createPromotionsPage.addSearchTrigger("animal");
        verifyThat(createPromotionsPage.finishButton(), not(hasAttribute("disabled")));

        finishPromotion();

        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining("animal").click();

        new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

        verifyThat(promotionsPage, containsText("animal"));
//        verifyThat(promotionsPage.promotionPosition(), containsText("2"));

    }

    @Test
    public void testPinToPositionSetPosition() {
        wizard.option("PIN_TO_POSITION").click();
        wizard.continueButton().click();
        wizard.loadOrFadeWait();

        createPromotionsPage.selectPositionPlusButton().click();
        verifyThat(createPromotionsPage.positionInputValue(), is(2));
        verifyThat(wizard.continueButton(), not(hasAttribute(("disabled"))));
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

        wizard.cancelButton().click();
        verifyThat(getDriver().getCurrentUrl(), not(containsString("create")));
    }

    @Test
    public void testAddRemoveTriggerTermsAndCancel() {
        createPromotionsPage.navigateToTriggers();
        assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
        assertThat("Trigger add button is not disabled when text box is empty", createPromotionsPage.isAttributePresent(createPromotionsPage.triggerAddButton(), "disabled"));
        assertThat("Trigger add button is not disabled when text box is empty", !createPromotionsPage.isAttributePresent(createPromotionsPage.cancelButton(CreateNewPromotionsBase.WizardStep.TRIGGER), "disabled"));

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

        createPromotionsPage.cancelButton(CreateNewPromotionsBase.WizardStep.TRIGGER).click();
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
        final String searchTrigger = "<h1>Hey</h1>";
        createPromotionsPage.addSearchTrigger(searchTrigger);

        final WebElement span = createPromotionsPage.findElement(By.cssSelector(".trigger-words-form .term"));
        assertThat("HTML was not escaped", span.getText().equals(searchTrigger));
    }

    @Test
    public void testNonNumericEntryInPinToPosition() {
        createPromotionsPage.promotionType("PIN_TO_POSITION").click();
        createPromotionsPage.continueButton(CreateNewPromotionsBase.WizardStep.TYPE).click();
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

            createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.continueButton(CreateNewPromotionsBase.WizardStep.PROMOTION_TYPE));
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
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining("delta").click();

        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

        verifyThat(promotionsPage, containsText("delta"));
        verifyThat(promotionsPage.promotionPosition().getValue(), is("2"));
    }

    @Test
    public void testAddSpotlightSponsored() {
        assumeThat(config.getType(), is(ApplicationType.ON_PREM));
        addSpotlightPromotion("Sponsored", "apples");
    }

    @Test
    public void testAddSpotlightHotwire() {
        assumeThat(config.getType(), is(ApplicationType.ON_PREM));
        addSpotlightPromotion("Hotwire", "grapes");
    }

    @Test
    public void testAddSpotlightTopPromotions() {
        assumeThat(config.getType(), is(ApplicationType.ON_PREM));
        addSpotlightPromotion("Top Promotions", "oranges");
    }

    @Test
    public void testHostedSpotlightPromotion() {
        assumeThat(config.getType(), is(ApplicationType.HOSTED));

        final String searchTrigger = "bananas";

        verifyThat(wizard.getTitle(), is("Promotion type"));
        verifyThat(wizard.continueButton(), hasAttribute("disabled"));
        wizard.option("SPOTLIGHT").click();
        verifyThat(wizard.continueButton(), not(hasAttribute("disabled")));

        wizard.continueButton().click();
        wizard.loadOrFadeWait();
        verifyThat(wizard.getTitle(), is("Promotion triggers"));
        verifyThat(wizard.finishButton(), hasAttribute("disabled"));

        wizard.formInput().setAndSubmit(searchTrigger);
        verifyThat(wizard.finishButton(), not(hasAttribute("disabled")));

        finishPromotion();
        searchPage.waitForLoad();
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

        new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
        verifyThat(promotionsPage, containsText("Spotlight for: " + searchTrigger));

        promotionsPage.clickableSearchTrigger(searchTrigger).click();
        searchPage.waitForLoad();
        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.loadOrFadeWait();

        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedDocTitle));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, not(containsText(promotedDocTitle)));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, containsText(promotedDocTitle));

        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions"));
        promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

        verifyThat(promotionsPage, containsText("Spotlight for: " + searchTrigger));
    }

    private void addSpotlightPromotion(final String spotlightType, final String searchTrigger) {
        verifyThat(wizard.getTitle(), is("Promotion type"));
        verifyThat(wizard.continueButton(), hasAttribute("disabled"));
        wizard.option("SPOTLIGHT").click();
        verifyThat(wizard.continueButton(), not(hasAttribute("disabled")));

        wizard.continueButton().click();
        wizard.loadOrFadeWait();
        verifyThat(wizard.getTitle(), is("Promotion details"));
        verifyThat(wizard.continueButton(), hasAttribute("disabled"));

        wizard.option(spotlightType).click();
        verifyThat(wizard.continueButton(), not(hasAttribute("disabled")));

        wizard.continueButton().click();
        wizard.loadOrFadeWait();
        verifyThat(wizard.getTitle(), is("Promotion triggers"));
        verifyThat(wizard.finishButton(), hasAttribute("disabled"));

        wizard.formInput().setAndSubmit(searchTrigger);
        verifyThat(wizard.finishButton(), not(hasAttribute("disabled")));

        finishPromotion();

        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

        new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
        verifyThat(promotionsPage, containsText("Spotlight for: " + searchTrigger));

        promotionsPage.clickableSearchTrigger(searchTrigger).click();
        promotionsPage.loadOrFadeWait();

        verifyThat(searchPage.getTopPromotedLinkTitle(), is(promotedDocTitle));
        verifyThat(searchPage.getTopPromotedLinkButtonText(), is(spotlightType));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, not(containsText(promotedDocTitle)));

        searchPage.modifiedResultsCheckBox().click();
        searchPage.loadOrFadeWait();
        verifyThat(searchPage, containsText(promotedDocTitle));

        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        verifyThat(getDriver().getCurrentUrl(), containsString("promotions"));
        promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

        verifyThat(promotionsPage, containsText("Spotlight for: " + searchTrigger));
        verifyThat(promotionsPage.spotlightButton(), containsText(spotlightType));
    }

    private void toggleAndCancel() {
        body.getTopNavBar().sideBarToggle();
        wizard.cancelButton().click();
        verifyThat(getDriver().getCurrentUrl(), containsString("search/modified"));
        verifyThat(searchPage.promotedItemsCount(), is(1));
        body.getTopNavBar().sideBarToggle();
        searchPage.promoteTheseItemsButton().click();
        createPromotionsPage.waitForLoad();
    }

    @Test
    public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
        verifyThat(getDriver().getCurrentUrl(), endsWith("promotions/create"));
        toggleAndCancel();
        SpotlightPromotion spotlight = new SpotlightPromotion("whatever");
        spotlight.doType(wizard);
        // TODO: refactor tests
        if (config.getType() == ApplicationType.ON_PREM) {
            verifyThat(wizard.getTitle(), is("Promotion details"));
            toggleAndCancel();

            for (final Promotion.SpotlightType spotlightType : Promotion.SpotlightType.values()) {
                SpotlightPromotion promotion = new SpotlightPromotion(spotlightType, "whatever");
                promotion.doType(wizard);
                promotion.doSpotlightType(wizard);
                verifyThat(wizard.getTitle(), is("Promotion triggers"));
                toggleAndCancel();
            }

            PinToPositionPromotion promotion = new PinToPositionPromotion(2, "whatever");
            promotion.doType(wizard);
            verifyThat(wizard.getTitle(), is("Promotion details"));
            toggleAndCancel();

            promotion.doType(wizard);
            promotion.doPosition(wizard);
        }
        verifyThat(wizard.getTitle(), is("Promotion triggers"));
        toggleAndCancel();
    }

    @Test
    public void testNotificationsForPromotions() {
        wizard.cancelButton().click();
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
            verifyThat(notifications.notificationNumber(1), containsText("Created a new spotlight promotion: Spotlight for: MyFirstNotification" + spotlightType.replaceAll("\\s+", "")));

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
