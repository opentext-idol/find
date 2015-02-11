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

public class EditDocumentReferencesPageITCase extends ABCTestBase {

	public EditDocumentReferencesPageITCase(final TestConfig config, final String browser, final Platform platform) {
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
	public void testNoMixUpBetweenSearchBucketAndEditPromotionsBucket() {
		final List<String> originalPromotedDocs = setUpANewMultiDocPromotion("English", "luke", "Hotwire", "jedi goodGuy", 8);
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

		searchPage.promoteTheseDocumentsButton().click();
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
		searchPage.promoteTheseDocumentsButton().click();

		final List<String> searchPageBucketDocs = searchPage.promotionsBucketList();

		for (final String bucketDoc : finalPromotionsBucketList) {
			assertFalse(searchPageBucketDocs.contains(bucketDoc));
		}
	}

	@Test
	public void testAddRemoveDocsToEditBucket() {
		setUpANewMultiDocPromotion("English", "yoda", "Hotwire", "green dude", 4);
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
		setUpANewPromotion("English", "Luke", "Hotwire", "Jedi Master");
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
		setUpANewPromotion("English", "Luke", "Hotwire", "Jedi Master");
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();

		assertThat("Page opens with an error message", !editReferences.getText().contains("An unknown error occurred executing the search action"));
		assertThat("Page opens with the wrong message", editReferences.getText().contains("Search for something to continue"));
		assertThat("Search items do not load", editReferences.saveButton().isDisplayed());
	}

	@Test
	public void testEditDocumentReferencesCancel() {
		final String originalDoc = setUpANewPromotion("English", "House", "Sponsored", "home");
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();

		editReferences.deleteDocFromWithinBucket(originalDoc);
		topNavBar.search("abode");
		editReferences.searchResultCheckbox(1).click();
		editReferences.searchResultCheckbox(2).click();
		editReferences.forwardPageButton().click();
		editReferences.searchResultCheckbox(3).click();
		editReferences.searchResultCheckbox(4).click();
		topNavBar.search("cottage");
		editReferences.searchResultCheckbox(5).click();
		editReferences.searchResultCheckbox(6).click();
		editReferences.cancelButton().click();

		assertEquals(1, promotionsPage.getPromotedList().size());
		assertThat("Original document is not attached to the promotion anymore", promotionsPage.getPromotedList().contains(originalDoc));

		promotionsPage.addMorePromotedItemsButton().click();
		promotionsPage.loadOrFadeWait();
		assertThat("Promotions bucket has kept unsaved changes", editReferences.promotionsBucketList().contains(originalDoc));
		assertEquals(1, editReferences.promotedItemsCount());
	}

	@Test
	public void testDeleteItemsFromWithinTheBucket() {
		setUpANewMultiDocPromotion("English", "yoda", "Hotwire", "green dude", 4);
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();
		editReferences.loadOrFadeWait();
		final List<String> bucketList = editReferences.promotionsBucketList();
		assertThat("There should be four documents in the bucket", bucketList.size() == 4);
		assertThat("save button not enabled when bucket has documents", !editReferences.saveButton().getAttribute("class").contains("disabled"));

		for (final String bucketDocTitle : bucketList) {
			final int docIndex = bucketList.indexOf(bucketDocTitle);
			editReferences.deleteDocFromWithinBucket(bucketDocTitle);
			assertThat("Document not removed from bucket", !editReferences.promotionsBucketList().contains(bucketDocTitle));
			assertThat("Wrong number of documents in the bucket", editReferences.promotionsBucketList().size() == 3 - docIndex);
		}

		assertThat("save button not disabled when bucket empty", editReferences.isAttributePresent(editReferences.saveButton(), "disabled"));
		editReferences.tryClickThenTryParentClick(editReferences.saveButton());
		assertThat("Save button not disabled when bucket empty", getDriver().getCurrentUrl().contains("promotions/edit"));
	}

	@Test
	public void testViewFromBucketAndFromSearchResults() throws InterruptedException {
		setUpANewMultiDocPromotion("French", "pomme", "Top Promotions", "potato", 35);
		promotionsPage.addMorePromotedItemsButton().click();
		promotionsPage.loadOrFadeWait();
		editReferences = body.getEditDocumentReferencesPage();

		for (int i = 0; i < 5; i++){
			final String handle = getDriver().getWindowHandle();
			final String docTitle = editReferences.promotionsBucketWebElements().get(i).getText();
			editReferences.getPromotionBucketElementByTitle(docTitle).click();

			Thread.sleep(5000);

			getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
			assertThat("View frame does not contain document", getDriver().findElement(By.xpath(".//*")).getText().contains(docTitle));

			getDriver().switchTo().window(handle);
			getDriver().findElement(By.xpath("//button[contains(@id, 'cboxClose')]")).click();
			editReferences.loadOrFadeWait();
		}

		topNavBar.search("ketchup");
		editReferences.loadOrFadeWait();

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 5; i++) {
				final String handle = getDriver().getWindowHandle();
				final String searchResultTitle = editReferences.getSearchResultTitle(i);
				editReferences.getSearchResult(i).click();

				Thread.sleep(5000);

				getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
				assertThat("View frame does not contain document", getDriver().findElement(By.xpath(".//*")).getText().contains(searchResultTitle));

				getDriver().switchTo().window(handle);
				getDriver().findElement(By.xpath("//button[contains(@id, 'cboxClose')]")).click();
				editReferences.loadOrFadeWait();
			}

			editReferences.forwardPageButton().click();
			editReferences.loadOrFadeWait();
		}

		editReferences.emptyBucket();
		topNavBar.search("frites");

		for (int i = 1; i < 5; i++) {
			editReferences.searchResultCheckbox(i).click();
			final String docTitle = editReferences.getSearchResultTitle(i);
			final String handle = getDriver().getWindowHandle();
			editReferences.getPromotionBucketElementByTitle(docTitle).click();

			Thread.sleep(5000);

			getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
			assertThat("View frame does not contain document", getDriver().findElement(By.xpath(".//*")).getText().contains(docTitle));

			getDriver().switchTo().window(handle);
			getDriver().findElement(By.xpath("//button[contains(@id, 'cboxClose')]")).click();
			editReferences.loadOrFadeWait();
		}
	}

	@Test
	public void testCheckboxUpdatesWithBucketDelete() {
		setUpANewMultiDocPromotion("English", "yoda", "Hotwire", "green dude", 4);
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();
		topNavBar.search("yoda");
		final List<String> bucketList = editReferences.promotionsBucketList();
		assertThat("There should be four documents in the bucket", bucketList.size() == 4);

		for (final String docTitle : bucketList) {
			editReferences.loadOrFadeWait();
			assertThat("Checkbox not selected", editReferences.searchResultCheckbox(docTitle).getAttribute("class").contains("checked"));
			editReferences.deleteDocFromWithinBucket(docTitle);
			assertThat("Checkbox still selected", !editReferences.searchResultCheckbox(docTitle).getAttribute("class").contains("checked"));
			assertThat("Document not removed from bucket", !editReferences.promotionsBucketList().contains(docTitle));
		}

		assertThat("save button not disabled when bucket empty", editReferences.isAttributePresent(editReferences.saveButton(), "disabled"));
		editReferences.tryClickThenTryParentClick(editReferences.saveButton());
		assertThat("Save button not disabled when bucket empty", getDriver().getCurrentUrl().contains("promotions/edit"));

		editReferences.searchResultCheckbox(6).click();
		final String newPromotedDoc = editReferences.getSearchResultTitle(6);

		editReferences.tryClickThenTryParentClick(editReferences.saveButton());
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
		assertThat("Save button not enabled when document in the bucket", getDriver().getCurrentUrl().contains("promotions/detail"));

		assertThat("newly promoted document does not appear in the list", promotionsPage.getPromotedList().contains(newPromotedDoc));
		assertThat("Documents have not been deleted", promotionsPage.getPromotedList().size() == 1);
	}

	@Test
	public void testDeletedDocumentsRemainDeleted() {
		setUpANewMultiDocPromotion("English", "sith", "Top Promotions", "naughty evil", 8);
		promotionsPage.addMorePromotedItemsButton().click();
		editReferences = body.getEditDocumentReferencesPage();
		editReferences.loadOrFadeWait();
		final List<String> bucketList = editReferences.promotionsBucketList();
		assertThat("There should be eight documents in the bucket", bucketList.size() == 8);

		for (int i = 0; i < 4; i++) {
			editReferences.deleteDocFromWithinBucket(bucketList.get(i));
			assertThat("Wrong number of documents in the bucket", editReferences.promotedItemsCount() == 7 - i);
		}

		editReferences.saveButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
		final List<String> promotionsList = promotionsPage.getPromotedList();

		for (int i = 0; i < 4; i++) {
			assertThat("Doc deleted in edit mode has not been deleted from promotions detail", !promotionsList.contains(bucketList.get(i)));
			assertThat("Doc not deleted in edit mode has been deleted from promotions detail", promotionsList.contains(bucketList.get(i + 4)));
		}
	}

	private String setUpANewPromotion(final String language, final String navBarSearchTerm, final String spotlightType, final String searchTriggers) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language);
		final String promotedDocTitle = searchPage.createAPromotion();
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();

		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers.split(" ")[0]).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
		return promotedDocTitle;
	}

	private List <String> setUpANewMultiDocPromotion(final String language, final String navBarSearchTerm, final String spotlightType, final String searchTriggers, final int numberOfDocs) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language);
		final List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion(numberOfDocs);
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
		return promotedDocTitles;
	}
}
