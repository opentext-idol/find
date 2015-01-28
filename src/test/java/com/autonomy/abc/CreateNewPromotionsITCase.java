package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.NotificationsDropDown;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

public class CreateNewPromotionsITCase extends ABCTestBase {

	public CreateNewPromotionsITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private SearchPage searchPage;
	private String promotedDocTitle;
	private PromotionsPage promotionsPage;
	private TopNavBar topNavBar;

	private CreateNewPromotionsPage createPromotionsPage;

	@Before
	public void setUp() {
		topNavBar = body.getTopNavBar();
		topNavBar.search("fox");
		searchPage = body.getSearchPage();
		promotedDocTitle = searchPage.createAPromotion();
		createPromotionsPage = body.getCreateNewPromotionsPage();
	}

	@After
	public void cleanUp() {
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testAddPinToPosition() {
		createPromotionsPage.promotionType("PIN_TO_POSITION").click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);
		assertThat("Minus button is not disabled when position equals 1", createPromotionsPage.isAttributePresent(createPromotionsPage.selectPositionMinusButton(), "disabled"));

		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Pin to position value not set to 2", createPromotionsPage.positionInputValue() == 2);
		assertThat("Minus button is not enabled when position equals 2", !createPromotionsPage.isAttributePresent(createPromotionsPage.selectPositionMinusButton(), "disabled"));

		createPromotionsPage.continueButton("pinToPosition").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.isAttributePresent(createPromotionsPage.finishButton(), "disabled"));

		createPromotionsPage.addSearchTrigger("animal");
		assertThat("Promote button is not disabled when no triggers are added", !createPromotionsPage.isAttributePresent(createPromotionsPage.finishButton(), "disabled"));

		createPromotionsPage.finishButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("animal").click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		assertThat("page does not have pin to position name", promotionsPage.getText().contains("animal"));
		assertThat("page does not have correct pin to position number", promotionsPage.getText().contains("Position: 2"));
	}

	@Test
	public void testPinToPositionSetPosition() {
		createPromotionsPage.promotionType("PIN_TO_POSITION").click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();

		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 2", createPromotionsPage.positionInputValue() == 2);
		assertThat("Minus button is not enabled when position equals 2", !createPromotionsPage.continueButton("pinToPosition").getAttribute("class").contains("disabled"));
		assertThat("Continue button is not enabled when position equals 2", !createPromotionsPage.selectPositionMinusButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 6", createPromotionsPage.positionInputValue() == 6);

		createPromotionsPage.selectPositionMinusButton().click();
		createPromotionsPage.selectPositionMinusButton().click();
		createPromotionsPage.selectPositionMinusButton().click();
		assertThat("Pin to position value not set to 3", createPromotionsPage.positionInputValue() == 3);

		try {
			// Try to send keys in case this gets changed back to an input
			createPromotionsPage.pinToPositionInput().sendKeys("16");
			assertThat("Pin to position value should not change", createPromotionsPage.positionInputValue() == 3);
		} catch (final WebDriverException e) {
			//try catch because chrome struggles to focus on this element
		}

		createPromotionsPage.cancelButton("pinToPosition").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

	@Test
	public void testAddRemoveTriggerTermsAndCancel() {
		createPromotionsPage.navigateToTriggers();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
		assertThat("Trigger add button is not disabled when text box is empty", createPromotionsPage.isAttributePresent(createPromotionsPage.triggerAddButton(), "disabled"));
		assertThat("Trigger add button is not disabled when text box is empty", !createPromotionsPage.isAttributePresent(createPromotionsPage.cancelButton("trigger"), "disabled"));

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

		createPromotionsPage.cancelButton("trigger").click();
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
		createPromotionsPage.continueButton("type").click();
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
			topNavBar.sideBarToggle();
			assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

			createPromotionsPage.pinToPositionInput().sendKeys("1");
			createPromotionsPage.selectPositionPlusButton().click();
			createPromotionsPage.pinToPositionInput().sendKeys("bad");
			assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 2);

			createPromotionsPage.tryClickThenTryParentClick(createPromotionsPage.continueButton("pinToPosition"));
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
		assertThat("Number of triggers does not equal 2", createPromotionsPage.getSearchTriggersList().size() == 2);

		createPromotionsPage.finishButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("delta").click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		assertThat("page does not have pin to position name", promotionsPage.getText().contains("delta"));
		assertThat("page does not have correct pin to position number", promotionsPage.getText().contains("Position: 2"));
	}

	@Test
	public void testAddSpotlightSponsored() {
		addSpotlightPromotion("Sponsored", "apples");
	}

	@Test
	public void testAddSpotlightHotwire() {
		addSpotlightPromotion("Hotwire", "grapes");
	}

	@Test
	public void testAddSpotlightTopPromotions() {
		addSpotlightPromotion("Top Promotions", "oranges");
	}

	private void addSpotlightPromotion(final String spotlightType, final String searchTrigger) {
		createPromotionsPage.promotionType("SPOTLIGHT").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Continue button not enabled", !createPromotionsPage.isAttributePresent(createPromotionsPage.continueButton("type"), "disabled"));

		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Continue button not disabled", createPromotionsPage.isAttributePresent(createPromotionsPage.continueButton("spotlightType"), "disabled"));

		createPromotionsPage.spotlightType(spotlightType).click();
		assertThat("Continue button not enabled", !createPromotionsPage.isAttributePresent(createPromotionsPage.continueButton("spotlightType"), "disabled"));

		createPromotionsPage.continueButton("spotlightType").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Promote button not disabled", createPromotionsPage.isAttributePresent(createPromotionsPage.finishButton(), "disabled"));

		createPromotionsPage.addSearchTrigger(searchTrigger);
		assertThat("Finish button not enabled", !createPromotionsPage.isAttributePresent(createPromotionsPage.finishButton(), "disabled"));

		createPromotionsPage.finishButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

		new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
		assertThat("Linked to wrong page", getDriver().getCurrentUrl().contains("promotions/detail/spotlight"));
		assertThat("Linked to wrong page", promotionsPage.getText().contains("Spotlight for: " + searchTrigger));

		promotionsPage.clickableSearchTrigger(searchTrigger).click();
		promotionsPage.loadOrFadeWait();

		assertThat("Wrong document spotlighted", createPromotionsPage.getTopPromotedLinkTitle().equals(promotedDocTitle));
		assertThat("Wrong spotlight button text", createPromotionsPage.getTopPromotedLinkButtonText().equals(spotlightType));

		searchPage.showHideUnmodifiedResults().click();
		searchPage.loadOrFadeWait();
		assertThat("Modified results have not been hidden", !searchPage.getText().contains(promotedDocTitle));

		searchPage.showHideUnmodifiedResults().click();
		searchPage.loadOrFadeWait();
		assertThat("Modified results have not been shown", searchPage.getText().contains(promotedDocTitle));

		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		assertThat("Linked to wrong page", getDriver().getCurrentUrl().contains("promotions"));
		promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

		assertThat("page does not have correct spotlight name", promotionsPage.getText().contains("Spotlight for: " + searchTrigger));
		assertThat("page does not have correct spotlight type", promotionsPage.spotlightButton().getText().contains(spotlightType));
	}

	@Test
	public void testWizardCancelButtonAfterClickingNavBarToggleButton() {
		assertThat("Incorrect URL", getDriver().getCurrentUrl().endsWith("promotions/create"));

		topNavBar.sideBarToggle();
		createPromotionsPage.cancelButton("type").click();
		assertThat("Cancel button does not work after navbar toggle", getDriver().getCurrentUrl().contains("search/modified"));
		assertThat("Items have not remained in the bucket", searchPage.promotedItemsCount() == 1);

		searchPage.promoteTheseItemsButton().click();
		createPromotionsPage.promotionType("SPOTLIGHT").click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Wrong section of wizard", createPromotionsPage.spotlightType("Sponsored").isDisplayed());

		topNavBar.sideBarToggle();
		createPromotionsPage.cancelButton("spotlightType").click();
		assertThat("Cancel button does not work after navbar toggle", getDriver().getCurrentUrl().contains("search/modified"));
		assertThat("Items have not remained in the bucket", searchPage.promotedItemsCount() == 1);

		for (final String spotlightType : Arrays.asList("Sponsored", "Hotwire", "Top Promotions")) {
			searchPage.promoteTheseItemsButton().click();
			createPromotionsPage.promotionType("SPOTLIGHT").click();
			createPromotionsPage.continueButton("type").click();
			createPromotionsPage.loadOrFadeWait();
			assertThat("Wrong section of wizard", createPromotionsPage.spotlightType(spotlightType).isDisplayed());

			createPromotionsPage.spotlightType(spotlightType).click();
			createPromotionsPage.continueButton("spotlightType").click();
			createPromotionsPage.loadOrFadeWait();
			assertThat("Wizard has not navigated forward", createPromotionsPage.triggerAddButton().isDisplayed());
			topNavBar.sideBarToggle();
			createPromotionsPage.cancelButton("trigger").click();
			assertThat("Cancel button does not work after navbar toggle", getDriver().getCurrentUrl().contains("search/modified"));
			assertThat("Items have not remained in the bucket", searchPage.promotedItemsCount() == 1);
		}

		searchPage.promoteTheseItemsButton().click();
		createPromotionsPage.promotionType("PIN_TO_POSITION").click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Wrong section of wizard", createPromotionsPage.selectPositionPlusButton().isDisplayed());

		topNavBar.sideBarToggle();
		createPromotionsPage.cancelButton("pinToPosition").click();
		assertThat("Cancel button does not work after navbar toggle", getDriver().getCurrentUrl().contains("search/modified"));
		assertThat("Items have not remained in the bucket", searchPage.promotedItemsCount() == 1);

		searchPage.loadOrFadeWait();
		searchPage.promoteTheseItemsButton().click();
		createPromotionsPage.promotionType("PIN_TO_POSITION").click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.continueButton("pinToPosition").click();
		createPromotionsPage.loadOrFadeWait();
		assertThat("Wizard has not navigated forward", createPromotionsPage.triggerAddButton().isDisplayed());

		topNavBar.sideBarToggle();
		createPromotionsPage.cancelButton("trigger").click();
		assertThat("Cancel button does not work after navbar toggle", getDriver().getCurrentUrl().contains("search/modified"));
		assertThat("Items have not remained in the bucket", searchPage.promotedItemsCount() == 1);
	}

	@Test
	public void testNotificationsForPromotions() throws InterruptedException {
		createPromotionsPage.cancelButton("type").click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.promotionsBucketClose();

		for (final String spotlightType : Arrays.asList("Sponsored", "Hotwire", "Top Promotions")) {
			topNavBar.search("dog");
			searchPage.createAPromotion();

			createPromotionsPage.addSpotlightPromotion(spotlightType, "MyFirstNotification" + spotlightType.replaceAll("\\s+", ""));
			searchPage.waitForGritterToClear();

			topNavBar.notificationsDropdown();
			final NotificationsDropDown notifications = body.getNotifications();
			topNavBar.loadOrFadeWait();
			assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Created a new spotlight promotion: Spotlight for: MyFirstNotification" + spotlightType.replaceAll("\\s+", "")));
			assertThat("User wrong in notification", notifications.notificationNumber(1).getText().contains(navBar.getSignedInUser()));

			notifications.notificationNumber(1).click();
			assertThat("notification link has not directed back to the promotions page", getDriver().getCurrentUrl().contains("promotions/detail/spotlight"));
		}
	}
}
