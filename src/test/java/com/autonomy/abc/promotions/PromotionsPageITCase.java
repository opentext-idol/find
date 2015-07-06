package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.promotions.CreateNewDynamicPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private SchedulePage schedulePage;
	private CreateNewDynamicPromotionsPage dynamicPromotionsPage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.newPromotionButton().click();
		assertThat("linked to wrong page", getDriver().getCurrentUrl().endsWith("promotions/new"));
		assertThat("linked to wrong page", body.getText().contains("Create New Promotion"));
	}

	@Test
	public void testCorrectDocumentsInPromotion() {
		final List<String> promotedDocTitles = promotionsPage.setUpANewMultiDocPromotion("English", "cars", "Sponsored", "wheels", 18, getConfig().getType().getName());
		final List<String> promotedList = promotionsPage.getPromotedList();

		for (final String title : promotedDocTitles) {
			assertThat("Promoted document title '" + title + "' does not match promoted documents on promotions page", promotedList.contains(title));
		}
	}

	@Test
	public void testDeletePromotedDocuments() {
		final List<String> promotedDocTitles = promotionsPage.setUpANewMultiDocPromotion("English", "cars", "Sponsored", "wheels", 5, getConfig().getType().getName());
		int numberOfDocuments = promotionsPage.getPromotedList().size();

		for (final String title : promotedDocTitles) {
			final String documentSummary = promotionsPage.promotedDocumentSummary(title);
			promotionsPage.deleteDocument(title);
			numberOfDocuments = numberOfDocuments - 1;

			if (numberOfDocuments > 1) {
				assertThat("A document with title '" + title + "' has not been deleted", promotionsPage.getPromotedList().size() == numberOfDocuments);
				assertThat("A document with title '" + title + "' has not been deleted", !promotionsPage.getPromotedList().contains(documentSummary));
			} else {
				assertThat("delete icon should be hidden when only one document remaining", promotionsPage.findElements(By.cssSelector(".remove-document-reference")).size() == 0);
				break;
			}
		}
	}

	@Test
	public void testWhitespaceTrigger() {
		promotionsPage.setUpANewPromotion("English", "cars", "Sponsored", "wheels", getConfig().getType().getName());

		promotionsPage.tryClickThenTryParentClick(promotionsPage.triggerAddButton());

		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);

		promotionsPage.addSearchTrigger("trigger");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("   ");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(" trigger");
		assertThat("Whitespace at beginning should be ignored", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\t");
		assertThat("Whitespace at beginning should be ignored", promotionsPage.getSearchTriggersList().size() == 2);
	}

	@Test
	public void testQuotesTrigger() throws InterruptedException {
		promotionsPage.setUpANewPromotion("English", "cars", "Sponsored", "wheels", getConfig().getType().getName());

		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);

		promotionsPage.addSearchTrigger("bag");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\"bag");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.waitForGritterToClear();
		promotionsPage.addSearchTrigger("bag\"");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);
	}

	@Test
	public void testCommasTrigger() {
		promotionsPage.setUpANewPromotion("English", "cars", "Sponsored", "wheels", getConfig().getType().getName());

		promotionsPage.addSearchTrigger("France");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(",Germany");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("Greece Romania");
		assertThat("New triggers not added", promotionsPage.getSearchTriggersList().size() == 4);
		assertThat("Error message still showing", !promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));
	}

	@Test
	public void testHTMLTrigger() {
		promotionsPage.setUpANewPromotion("English", "vans", "Sponsored", "shoes", getConfig().getType().getName());
		final String trigger = "<h1>Hi</h1>";
		promotionsPage.addSearchTrigger(trigger);

		assertThat("Triggers should be HTML escaped", promotionsPage.getSearchTriggersList().contains(trigger));
	}

	@Test
	public void testAddRemoveTriggers() throws InterruptedException {
		promotionsPage.setUpANewPromotion("English", "cars", "Sponsored", "wheels", getConfig().getType().getName());
		promotionsPage.addSearchTrigger("alpha");
		promotionsPage.removeSearchTrigger("wheels");
		assertThat("Number of search terms does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Original search trigger 'wheels has not been deleted'", !promotionsPage.getSearchTriggersList().contains("wheels"));

		promotionsPage.addSearchTrigger("beta gamma delta");
		promotionsPage.waitForGritterToClear();
		promotionsPage.removeSearchTrigger("gamma");
		promotionsPage.removeSearchTrigger("alpha");
		promotionsPage.addSearchTrigger("epsilon");
		promotionsPage.removeSearchTrigger("beta");

		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("Trigger 'delta' not present", promotionsPage.getSearchTriggersList().contains("delta"));
		assertThat("Trigger 'epsilon' not present", promotionsPage.getSearchTriggersList().contains("epsilon"));

		promotionsPage.removeSearchTrigger("epsilon");
		assertThat("It should not be possible to delete the last trigger", promotionsPage.findElements(By.cssSelector(".remove-word")).size() == 0);
	}

	@Test
	public void testBackButton() {
		promotionsPage.setUpANewPromotion("English", "bicycle", "Sponsored", "tyres", getConfig().getType().getName());
		promotionsPage.backButton().click();
		assertThat("Back button does not redirect to main promotions page", getDriver().getCurrentUrl().endsWith("promotions"));
	}

	@Test
	public void testEditPromotionName() throws InterruptedException {
		promotionsPage.setUpANewPromotion("English", "cars", "Sponsored", "wheels", getConfig().getType().getName());
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("Spotlight for: wheels"));

		promotionsPage.createNewTitle("Fuzz");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("Fuzz"));

		promotionsPage.createNewTitle("<script> alert(\"hi\") </script>");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("<script> alert(\"hi\") </script>"));
	}

	@Test
	public void testEditPromotionType() {
		promotionsPage.setUpANewPromotion("English", "cars", "Sponsored", "wheels", getConfig().getType().getName());
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Sponsored"));

		promotionsPage.changeSpotlightType("Hotwire");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Hotwire"));

		promotionsPage.changeSpotlightType("Top Promotions");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Top Promotions"));

		promotionsPage.changeSpotlightType("Sponsored");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Sponsored"));
	}

	@Test
	public void testDeletePromotions() throws InterruptedException {
		promotionsPage.setUpANewPromotion("English", "rabbit", "Sponsored", "bunny", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewPromotion("English", "horse", "Sponsored", "pony", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewPromotion("English", "dog", "Sponsored", "<script> document.body.innerHTML = '' </script>", getConfig().getType().getName());
		promotionsPage.backButton().click();

		assertThat("promotion 'bunny' not created", promotionsPage.getPromotionLinkWithTitleContaining("bunny").isDisplayed());
		assertThat("promotion 'pony' not created", promotionsPage.getPromotionLinkWithTitleContaining("pony").isDisplayed());
		assertThat("promotion 'pooch' not created", promotionsPage.getPromotionLinkWithTitleContaining("script").isDisplayed());
		assertThat("promotion 'pooch' not created", promotionsPage.promotionsList().size() == 3);

		promotionsPage.getPromotionLinkWithTitleContaining("bunny").click();
		body.waitForGritterToClear();
		promotionsPage.deletePromotion();

		assertThat("promotion 'pony' has been deleted", promotionsPage.getPromotionLinkWithTitleContaining("pony").isDisplayed());
		assertThat("promotion 'script' has been deleted", promotionsPage.getPromotionLinkWithTitleContaining("script").isDisplayed());
		assertThat("promotion 'bunny' not deleted", promotionsPage.promotionsList().size() == 2);

		promotionsPage.getPromotionLinkWithTitleContaining("script").click();
		promotionsPage.deletePromotion();

		assertThat("promotion 'pony' has been deleted", promotionsPage.getPromotionLinkWithTitleContaining("pony").isDisplayed());
		assertThat("promotion 'script' not deleted", promotionsPage.promotionsList().size() == 1);

		promotionsPage.getPromotionLinkWithTitleContaining("pony").click();
		promotionsPage.deletePromotion();

		assertThat("promotion 'pony' not deleted", promotionsPage.promotionsList().size() == 0);
	}

	@Test
	public void testAddingLotsOfDocsToAPromotion() {
		promotionsPage.setUpANewMultiDocPromotion("English", "sith", "Hotwire", "darth sith", 100, getConfig().getType().getName());
		assertEquals("Wrong number of documents in the promotions list", 100, promotionsPage.getPromotedList().size());
	}

	@Test
	public void testPromotionFilter() throws InterruptedException {
		promotionsPage.setUpANewPromotion("French", "chien", "Hotwire", "woof bark", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewPromotion("Arabic", "الكلب", "Top Promotions", "dog chien", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewPromotion("English", "dog", "Sponsored", "hound pooch", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "woof swahili", 3, getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "pooch swahili", 3, getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewDynamicPromotion("Afrikaans", "hond", "pooch hond wolf", "Hotwire", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.setUpANewDynamicPromotion("Afrikaans", "hond", "lupo wolf", "Sponsored", getConfig().getType().getName());
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();
		assertEquals(7, promotionsPage.promotionsList().size());

		List<WebElement> promotions = promotionsPage.promotionsList();
		for (int i = 0; i < promotions.size() - 1; i++) {
			assertTrue("Following promotions not in alphabetical order: " + promotions.get(i).getText().toLowerCase().split("\\n")[1] + ", " + promotions.get(i + 1).getText().toLowerCase().split("\\n")[1], promotions.get(i).getText().toLowerCase().split("\\n")[1].compareTo(promotions.get(i + 1).getText().toLowerCase().split("\\n")[1]) <= 0);
		}

		promotionsPage.getPromotionLinkWithTitleContaining(promotions.get(3).getText().split("\\n")[1]).click();
		promotionsPage.createNewTitle("aaa");
		promotionsPage.loadOrFadeWait();
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();

		final List<WebElement> promotionsAgain = promotionsPage.promotionsList();
		for (int i = 0; i < promotionsAgain.size() - 1; i++) {
			assertTrue("Following promotions not in alphabetical order: " + promotionsAgain.get(i).getText().toLowerCase().split("\\n")[1] + ", " + promotionsAgain.get(i + 1).getText().toLowerCase().split("\\n")[1], promotionsAgain.get(i).getText().split("\\n")[1].toLowerCase().compareTo(promotionsAgain.get(i + 1).getText().split("\\n")[1].toLowerCase()) <= 0);
		}

		promotionsPage.getPromotionLinkWithTitleContaining(promotions.get(3).getText().split("\\n")[1]).click();
		promotionsPage.createNewTitle(promotions.get(3).getText());
		promotionsPage.loadOrFadeWait();
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.promotionsSearchFilter().sendKeys("dog");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(3, promotionsPage.promotionsList().size());
		promotions = promotionsPage.promotionsList();
		for (int i = 0; i < promotions.size() - 1; i++) {
			assertTrue("Following promotions not in alphabetical order: " + promotions.get(i).getText().split("\\n")[1] + ", " + promotions.get(i + 1).getText().split("\\n")[1], promotions.get(i).getText().split("\\n")[1].toLowerCase().compareTo(promotions.get(i + 1).getText().split("\\n")[1].toLowerCase()) <= 0);
		}

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("hound");
		assertThat("title has not changed", promotionsPage.getPromotionTitle().equals("hound"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(3, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("pooch");
		assertThat("trigger not removed", !promotionsPage.getSearchTriggersList().contains("pooch"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(2, promotionsPage.promotionsList().size());

		assertEquals("All Types", promotionsPage.promotionsCategoryFilterValue());

		promotionsPage.selectPromotionsCategoryFilter("Spotlight");
		promotionsPage.clearPromotionsSearchFilter();
		assertEquals("Spotlight", promotionsPage.promotionsCategoryFilterValue());
		assertEquals(3, promotionsPage.promotionsList().size());

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.selectPromotionsCategoryFilter("Pin to Position");
		promotionsPage.clearPromotionsSearchFilter();
		assertEquals("Pin to Position", promotionsPage.promotionsCategoryFilterValue());
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("lupo").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("wolf");
		assertThat("trigger not removed", !promotionsPage.getSearchTriggersList().contains("wolf"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("lupo").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("lupo");
		assertThat("title has not changed", promotionsPage.getPromotionTitle().equals("lupo"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("hond").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.addSearchTrigger("Rhodesian Ridgeback");
		assertThat("trigger not added", promotionsPage.getSearchTriggersList().containsAll(Arrays.asList("Rhodesian", "Ridgeback")));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("Rhodesian");
		assertEquals("Filter should have returned one document", 1, promotionsPage.promotionsList().size());

		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("Ridgeback");
		assertEquals(1, promotionsPage.promotionsList().size());
	}

	@Test
	public void testPromotionLanguages() {
		promotionsPage.setUpANewPromotion("French", "chien", "Hotwire", "woof bark", getConfig().getType().getName());
		assertEquals("French", promotionsPage.getLanguage());

		promotionsPage.backButton().click();
		promotionsPage.setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "swahili woof", 3, getConfig().getType().getName());
		assertEquals("Swahili", promotionsPage.getLanguage());

		promotionsPage.backButton().click();
		promotionsPage.setUpANewDynamicPromotion("Afrikaans", "pooch", "hond wolf", "Hotwire", getConfig().getType().getName());
		assertEquals("Afrikaans", promotionsPage.getLanguage());
	}

	@Test
	public void testEditDynamicQuery() throws InterruptedException {
		topNavBar.search("kitty");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("French", getConfig().getType().getName());
		final String firstSearchResult = searchPage.getSearchResult(1).getText();
		final String secondSearchResult = promotionsPage.setUpANewDynamicPromotion("French", "chat", "Meow", "Top Promotions", getConfig().getType().getName());
		promotionsPage.addSearchTrigger("purrr");
		assertFalse("Inconsistent changing of case by create promotions wizard", promotionsPage.getSearchTriggersList().contains("meow"));
		promotionsPage.removeSearchTrigger("Meow");
		topNavBar.search("purrr");
		searchPage.selectLanguage("French", getConfig().getType().getName());
		assertEquals(secondSearchResult, searchPage.promotionsSummaryList(false).get(0));

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.loadOrFadeWait();
		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.loadOrFadeWait();
		promotionsPage.getPromotionLinkWithTitleContaining("Meow").click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assertEquals("chat", promotionsPage.getQueryText());

		promotionsPage.editQueryText("kitty");
		assertEquals("kitty", promotionsPage.getQueryText());

		topNavBar.search("purrr");
		searchPage.selectLanguage("French", getConfig().getType().getName());
		assertEquals(firstSearchResult, searchPage.promotionsSummaryList(false).get(0));

		getDriver().navigate().refresh();
		searchPage = body.getSearchPage();
		searchPage.loadOrFadeWait();
		assertEquals(firstSearchResult, searchPage.promotionsSummaryList(false).get(0));
	}

	@Test
	public void testPromotionCreationAndDeletionOnSecondWindow() {
		promotionsPage.setUpANewPromotion("French", "chien", "Hotwire", "woof bark", getConfig().getType().getName());

		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = promotionsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final AppBody secondBody = new AppBody(getDriver());
		final PromotionsPage secondPromotionsPage = secondBody.getPromotionsPage();
		secondPromotionsPage.loadOrFadeWait();
		assertThat("Haven't navigated to promotions menu", secondPromotionsPage.newPromotionButton().isDisplayed());

		getDriver().switchTo().window(browserHandles.get(0));
		promotionsPage = body.getPromotionsPage();
		promotionsPage.loadOrFadeWait();
		promotionsPage.setUpANewDynamicPromotion("Swahili", "rafiki", "friend", "Sponsored", getConfig().getType().getName());

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(2, secondPromotionsPage.promotionsList().size());

		getDriver().switchTo().window(browserHandles.get(0));
		promotionsPage.deletePromotion();

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(1, secondPromotionsPage.promotionsList().size());

		secondPromotionsPage.getPromotionLinkWithTitleContaining("woof").click();
		secondPromotionsPage.deletePromotion();

		getDriver().switchTo().window(browserHandles.get(0));
		assertThat("Promotion not deleted", promotionsPage.getText().contains("There are no promotions..."));
	}

	@Test
	public void testPromotionFieldTextRestriction() {
		promotionsPage.setUpANewPromotion("English", "hot", "Hotwire", "hot", getConfig().getType().getName());

		promotionsPage.addFieldText("MATCH{hot}:DRECONTENT");

		topNavBar.search("hot");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot pot");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hots");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("hot").click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.fieldTextRemoveButton().click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextAddButton()));

		topNavBar.search("hot");
		searchPage.loadOrFadeWait();
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot chocolate");
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hots");
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("hot").click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.fieldTextAddButton().click();
		promotionsPage.fieldTextInputBox().sendKeys("<h1>hi</h1>");
		promotionsPage.fieldTextTickConfirmButton().click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
		assertEquals("<h1>hi</h1>", promotionsPage.fieldTextValue());

		promotionsPage.fieldTextEditButton().click();
		promotionsPage.fieldTextInputBox().clear();
		promotionsPage.fieldTextInputBox().sendKeys("MATCH{hot dog}:DRECONTENT");
		promotionsPage.fieldTextTickConfirmButton().click();
		promotionsPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));

		topNavBar.search("hot dog");
		searchPage.loadOrFadeWait();
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot chocolate");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("dog");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot dogs");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
	}

	@Test
	public void testPromotionFieldTextOrRestriction() {
		promotionsPage.setUpANewPromotion("English", "road", "Hotwire", "highway street", getConfig().getType().getName());

		promotionsPage.addFieldText("MATCH{highway}:DRECONTENT OR MATCH{street}:DRECONTENT");

		topNavBar.search("highway street");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("road");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("ROAD");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highway");
		assertThat("Promoted documents are not visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("street");
		assertThat("Promoted documents are not visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highway street");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("street highway");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("street street");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highwaystreet");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highway AND street");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
	}

	@Test
	public void testFieldTextSubmitTextOnEnter() {
		promotionsPage.setUpANewPromotion("English", "road", "Hotwire", "highway street", getConfig().getType().getName());

		promotionsPage.fieldTextAddButton().click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.fieldTextInputBox().sendKeys("TEST");
		promotionsPage.fieldTextInputBox().sendKeys(Keys.RETURN);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
		assertTrue("Field text cannot be submitted with an enter key", promotionsPage.fieldTextRemoveButton().isDisplayed());
	}

	@Test
	public void testCreateFieldTextField() {
		promotionsPage.setUpANewPromotion("Telugu", "మింగ్ వంశము", "Top Promotions", "Ming", getConfig().getType().getName());

		promotionsPage.addFieldText("MATCH{Richard}:NAME");
		topNavBar.search("Ming");
		searchPage = body.getSearchPage();
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		searchPage.expandFilter(SearchBase.Filter.FIELD_TEXT);
		searchPage.loadOrFadeWait();
		searchPage.fieldTextAddButton().click();
		searchPage.fieldTextInput().sendKeys("MATCH{Richard}:NAME");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
	}

	@Test
	public void testCountSearchResultsWithPinToPositionInjected() {
		promotionsPage.setUpANewMultiDocPinToPositionPromotion("French", "Lyon", "boeuf frites orange", 13, getConfig().getType().getName());
		for (final String query : Arrays.asList("boeuf", "frites", "orange")) {
			topNavBar.search(query);
			searchPage.selectLanguage("French", getConfig().getType().getName());
			final int initialStatedNumberOfResults = searchPage.countSearchResults();
			searchPage.forwardToLastPageButton().click();
			searchPage.loadOrFadeWait();
			final int numberOfPages = searchPage.getCurrentPageNumber();
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			assertEquals((numberOfPages - 1) * 6 + lastPageDocumentsCount, searchPage.countSearchResults());
			assertEquals(initialStatedNumberOfResults, searchPage.countSearchResults());
		}
	}
}
