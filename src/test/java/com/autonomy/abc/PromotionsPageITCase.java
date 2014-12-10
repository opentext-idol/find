package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private EditDocumentReferencesPage editReferences;

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
		final List<String> promotedDocTitles = setUpANewMultiDocPromotion("cars", "Sponsored", "wheels", 18);
		final List<String> promotedList = promotionsPage.getPromotedList();

		for (final String title : promotedDocTitles) {
			assertThat("Promoted document title '" + title + "' does not match promoted documents on promotions page", promotedList.contains(title));
		}
	}

	@Test
	public void testDeletePromotedDocuments() {
		final List<String> promotedDocTitles = setUpANewMultiDocPromotion("cars", "Sponsored", "wheels", 5);
		int numberOfDocuments = promotionsPage.getPromotedList().size();

		for (final String title : promotedDocTitles) {
			final String documentSummary = promotionsPage.promotedDocumentSummary(title);
			promotionsPage.deleteDocument(title);
			numberOfDocuments = numberOfDocuments - 1;

			if (numberOfDocuments > 1) {
				assertThat("A document with title '" + title + "' has not been deleted", promotionsPage.getPromotedList().size() == numberOfDocuments);
				assertThat("A document with title '" + title + "' has not been deleted", !promotionsPage.getPromotedList().contains(documentSummary));
			} else {
				assertThat("delete icon should be hidden when only one document remaining", !promotionsPage.findElement(By.cssSelector(".remove-document-reference")).isDisplayed());
				break;
			}
		}
	}

	@Test
	public void testWhitespaceTrigger() {
		setUpANewPromotion("cars", "Sponsored", "wheels");

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
		setUpANewPromotion("cars", "Sponsored", "wheels");

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
		setUpANewPromotion("cars", "Sponsored", "wheels");

		promotionsPage.addSearchTrigger("France");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(",Germany");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("Greece Romania");
		assertThat("New triggers not added", promotionsPage.getSearchTriggersList().size() == 4);
		assertThat("Error message still showing", !promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));
	}

	@Test
	public void testHTMLTrigger() {
		setUpANewPromotion("vans", "Sponsored", "shoes");
		final String trigger = "<h1>Hi</h1>";
		promotionsPage.addSearchTrigger(trigger);

		assertThat("Triggers should be HTML escaped", promotionsPage.getSearchTriggersList().contains(trigger));
	}

	@Test
	public void testAddRemoveTriggers() throws InterruptedException {
		setUpANewPromotion("cars", "Sponsored", "wheels");
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
		setUpANewPromotion("bicycle", "Sponsored", "tyres");
		promotionsPage.backButton().click();
		assertThat("Back button does not redirect to main promotions page", getDriver().getCurrentUrl().endsWith("promotions"));
	}

	@Test
	public void testEditPromotionName() throws InterruptedException {
		setUpANewPromotion("cars", "Sponsored", "wheels");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("Spotlight for: wheels"));

		promotionsPage.createNewTitle("Fuzz");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("Fuzz"));

		promotionsPage.createNewTitle("<script> alert(\"hi\") </script>");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("<script> alert(\"hi\") </script>"));
	}

	@Test
	public void testEditPromotionType() {
		setUpANewPromotion("cars", "Sponsored", "wheels");
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
		setUpANewPromotion("rabbit", "Sponsored", "bunny");
		promotionsPage.backButton().click();
		setUpANewPromotion("horse", "Sponsored", "pony");
		promotionsPage.backButton().click();
		setUpANewPromotion("dog", "Sponsored", "<script> alert('hi') </script>");
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
	public void testNoMixUpBetweenSearchBucketAndEditPromotionsBucket() {
		final List<String> originalPromotedDocs = setUpANewMultiDocPromotion("luke", "Hotwire", "jedi goodGuy", 8);
		final List<String> promotedDocs = promotionsPage.getPromotedList();
		promotionsPage.addMorePromotedItemsButton().click();
		promotionsPage.loadOrFadeWait();
		editReferences = body.getEditDocumentReferencesPage();
		final List<String> promotionsBucketList = editReferences.promotionsBucketList();

		assertEquals(originalPromotedDocs.size(), promotedDocs.size());
		assertEquals(promotionsBucketList.size(), promotedDocs.size());

		for (final String docTitle : promotionsBucketList) {
			assertTrue(promotedDocs.contains(docTitle));
		}

		navBar.switchPage(NavBarTabId.OVERVIEW);
		topNavBar.search("edit");

		searchPage.promoteButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.searchResultCheckbox(2).click();
		searchPage.searchResultCheckbox(3).click();
		searchPage.loadOrFadeWait();
		final List<String> searchBucketDocs = searchPage.promotionsBucketList();

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("jedi").click();
		promotionsPage.addMorePromotedItemsButton().click();
		promotionsPage.loadOrFadeWait();

		final List<String> secondPromotionsBucketList = editReferences.promotionsBucketList();
		assertEquals(promotionsBucketList.size(), secondPromotionsBucketList.size());

		for (final String searchBucketDoc : searchBucketDocs) {
			assertFalse(secondPromotionsBucketList.contains(searchBucketDoc));
		}

		topNavBar.search("wall");
		editReferences.searchResultCheckbox(1);
		editReferences.searchResultCheckbox(2);

		final List<String> finalPromotionsBucketList = editReferences.promotionsBucketList();

		navBar.switchPage(NavBarTabId.OVERVIEW);
		topNavBar.search("fast");
		searchPage.promoteButton().click();

		final List<String> searchPageBucketDocs = searchPage.promotionsBucketList();

		for (final String bucketDoc : finalPromotionsBucketList) {
			assertFalse(searchPageBucketDocs.contains(bucketDoc));
		}
	}

	@Test
	public void testAddRemoveDocsToEditBucket() {
		setUpANewMultiDocPromotion("yoda", "Hotwire", "green dude", 4);
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();

		assertEquals(4, editReferences.promotedItemsCount());

		topNavBar.search("star");

		for (int i = 1; i < 7; i++) {
			AppElement.scrollIntoView(editReferences.searchResultCheckbox(i), getDriver());
			editReferences.searchResultCheckbox(i).click();
			assertThat("Promoted items count should equal " + String.valueOf(i), editReferences.promotedItemsCount() == i + 4);
		}

		for (int j = 6; j > 0; j--) {
			AppElement.scrollIntoView(editReferences.searchResultCheckbox(j), getDriver());
			editReferences.searchResultCheckbox(j).click();
			assertThat("Promoted items count should equal " + String.valueOf(j), editReferences.promotedItemsCount() == j - 1 + 4);
		}
	}

	@Test
	public void testRefreshEditPromotionPage() throws InterruptedException {
		setUpANewPromotion("Luke", "Hotwire", "Jedi Master");
		promotionsPage.addMorePromotedItemsButton().click();
		getDriver().navigate().refresh();
		Thread.sleep(3000);

		try {
			assertThat("After refresh page elements not visible", getDriver().findElement(By.cssSelector(".page-container")).getText().contains("Edit Document References"));
		} catch (final StaleElementReferenceException e) {
			assertThat("After refresh page elements not visible", false);
		}
	}

	@Test
	public void testErrorMessageOnStartUpEditReferencesPage() {
		setUpANewPromotion("Luke", "Hotwire", "Jedi Master");
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();

		assertThat("Page opens with an error message", !editReferences.getText().contains("An unknown error occurred executing the search action"));
		assertThat("Page opens with the wrong message", editReferences.getText().contains("Search for something to continue"));
		assertThat("Search items do not load", editReferences.saveButton().isDisplayed());
	}

	private String setUpANewPromotion(final String navBarSearchTerm, final String spotlightType, final String searchTriggers) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		final String promotedDocTitle = searchPage.createAPromotion();
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();

		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers.split(" ")[0]).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
		return promotedDocTitle;
	}

	private List <String> setUpANewMultiDocPromotion(final String navBarSearchTerm, final String spotlightType, final String searchTriggers, final int numberOfDocs) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		final List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion(numberOfDocs);
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
		return promotedDocTitles;
	}
}
