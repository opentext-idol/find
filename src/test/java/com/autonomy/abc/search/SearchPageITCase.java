package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class SearchPageITCase extends ABCTestBase {
	public SearchPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;
	private CreateNewPromotionsPage createPromotionsPage;
	private PromotionsPage promotionsPage;
	DatePicker datePicker;

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = body.getSearchPage();
	}

	@Test
	public void testUnmodifiedResultsToggleButton(){
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Show unmodified results"));
		assertThat("Url incorrect", getDriver().getCurrentUrl().contains("/modified"));

		searchPage.showHideUnmodifiedResults().click();
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Showing unmodified results"));
		assertThat("Url incorrect", getDriver().getCurrentUrl().contains("/unmodified"));

		searchPage.showHideUnmodifiedResults().click();
		assertThat("Button toggle wrong", searchPage.showHideUnmodifiedResults().getText().equals("Show unmodified results"));
		assertThat("Url incorrect", getDriver().getCurrentUrl().contains("/modified"));
	}

	@Test
	public void testSearch(){
		topNavBar.search("dog");
		assertThat("Search title text is wrong. Expected: Dog  Returned: " + searchPage.searchTitle().getText(), searchPage.searchTitle().getText().equals("dog"));

		topNavBar.search("cat");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("cat"));

		topNavBar.search("ElEPhanT");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("ElEPhanT"));
	}

	@Test
	public void testPromoteButton(){
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be visible", !searchPage.promoteTheseItemsButton().isDisplayed());
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount() == 0);

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promote these items button should be visible", searchPage.promoteTheseItemsButton().isDisplayed());
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 1);

		searchPage.promotionsBucketClose();
		assertThat("Promoted items bucket has not appeared", !searchPage.getText().contains("Select Items to Promote"));

		searchPage.promoteTheseDocumentsButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be visible", !searchPage.promoteTheseItemsButton().isDisplayed());
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount() == 0);
	}

	@Test
	public void testAddFilesToPromoteBucket() {
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.loadOrFadeWait();

		for (int i = 1; i < 7; i++) {
			AppElement.scrollIntoView(searchPage.searchResultCheckbox(i), getDriver());
			searchPage.searchResultCheckbox(i).click();
			assertThat("Promoted items count should equal " + String.valueOf(i), searchPage.promotedItemsCount() == i);
		}

		for (int j = 6; j > 0; j--) {
			AppElement.scrollIntoView(searchPage.searchResultCheckbox(j), getDriver());
			searchPage.searchResultCheckbox(j).click();
			assertThat("Promoted items count should equal " + String.valueOf(j), searchPage.promotedItemsCount() == j - 1);
		}

		searchPage.promotionsBucketClose();
	}

	@Test
	public void testSearchResultsPagination() {
		topNavBar.search("dog");
		searchPage.loadOrFadeWait();
		assertThat("Back to first page button is not disabled", searchPage.isBackToFirstPageButtonDisabled());
		assertThat("Back a page button is not disabled", AppElement.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));

		searchPage.javascriptClick(searchPage.forwardPageButton());
		searchPage.paginateWait();
		assertThat("Back to first page button is not enabled", !AppElement.getParent(searchPage.backToFirstPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Back a page button is not enabled", !AppElement.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Page 2 is not active", searchPage.isPageActive(2));

		searchPage.javascriptClick(searchPage.forwardPageButton());
		searchPage.paginateWait();
		searchPage.javascriptClick(searchPage.forwardPageButton());
		searchPage.paginateWait();
		searchPage.javascriptClick(searchPage.backPageButton());
		searchPage.paginateWait();
		assertThat("Page 3 is not active", searchPage.isPageActive(3));

		searchPage.javascriptClick(searchPage.backToFirstPageButton());
		searchPage.paginateWait();
		assertThat("Page 1 is not active", searchPage.isPageActive(1));

		searchPage.javascriptClick(searchPage.forwardToLastPageButton());
		searchPage.paginateWait();
		assertThat("Forward to last page button is not disabled", AppElement.getParent(searchPage.forwardToLastPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward a page button is not disabled", AppElement.getParent(searchPage.forwardPageButton()).getAttribute("class").contains("disabled"));

		final int numberOfPages = searchPage.getCurrentPageNumber();

		for (int i = numberOfPages - 1; i > 0; i--) {
			searchPage.javascriptClick(searchPage.backPageButton());
			searchPage.paginateWait();
			assertThat("Page " + String.valueOf(i) + " is not active", searchPage.isPageActive(i));
			assertThat("Url incorrect", getDriver().getCurrentUrl().endsWith(String.valueOf(i)));
		}

		for (int j = 2; j < numberOfPages + 1; j++) {
			searchPage.javascriptClick(searchPage.forwardPageButton());
			searchPage.paginateWait();
			assertThat("Page " + String.valueOf(j) + " is not active", searchPage.isPageActive(j));
			assertThat("Url incorrect", getDriver().getCurrentUrl().endsWith(String.valueOf(j)));
		}
	}

	// This used to fail because the predict=false parameter was not added to our query actions
	@Test
	public void testPaginationAndBackButton() {
		topNavBar.search("safe");
		searchPage.forwardToLastPageButton().click();
		assertThat("Forward to last page button is not disabled", AppElement.getParent(searchPage.forwardToLastPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward a page button is not disabled", AppElement.getParent(searchPage.forwardPageButton()).getAttribute("class").contains("disabled"));
		final int lastPage = searchPage.getCurrentPageNumber();

		getDriver().navigate().back();
		assertThat("Back button has not brought the user back to the first page", searchPage.getCurrentPageNumber() == 1);

		getDriver().navigate().forward();
		assertThat("Forward button has not brought the user back to the last page", searchPage.getCurrentPageNumber() == lastPage);
	}

	@Test
	public void testAddDocumentToPromotionsBucket() {
		topNavBar.search("horse");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 1);
		assertThat("File in bucket description does not match file added", searchPage.getSearchResultTitle(1).equals(searchPage.bucketDocumentTitle(1)));
	}

	@Test
	public void testPromoteTheseItemsButtonLink() {
		topNavBar.search("fox");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.promoteTheseItemsButton().click();
		assertThat("Create new promotions page not open", getDriver().getCurrentUrl().endsWith("promotions/create"));
	}

	@Test
	public void testMultiDocPromotionDrawerExpandAndPagination() {
		topNavBar.search("freeze");
		searchPage.createAMultiDocumentPromotion(18);
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat", getConfig().getType().getName());

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("boat").click();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		promotionsPage.clickableSearchTrigger("boat").click();
		promotionsPage.loadOrFadeWait();
		assertThat("Summary size should equal 2", searchPage.getPromotionSummarySize() == 2);

		assertTrue("The show more promotions button is not visible", searchPage.showMorePromotionsButton().isDisplayed());
		searchPage.showMorePromotions();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 5);

		searchPage.showLessPromotions();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 2);

		searchPage.showMorePromotions();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 5);

		assertThat("Back to start button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));
		assertThat("Back button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward to end button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryForwardButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back to start button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));
		assertThat("Back button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward to end button should be enabled", !AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryForwardButton().click();
		searchPage.loadOrFadeWait();
		searchPage.promotionSummaryForwardButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back to start button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));
		assertThat("Back button should be enabled", !AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward button should be disabled", AppElement.getParent(searchPage.promotionSummaryForwardButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward to end button should be disabled", AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryBackButton().click();
		searchPage.loadOrFadeWait();
		searchPage.promotionSummaryBackButton().click();
		searchPage.loadOrFadeWait();
		searchPage.promotionSummaryBackButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back to start button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackToStartButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryForwardToEndButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Forward to end button should be disabled", AppElement.getParent(searchPage.promotionSummaryForwardToEndButton()).getAttribute("class").contains("disabled"));

		searchPage.promotionSummaryBackToStartButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Back button should be disabled", AppElement.getParent(searchPage.promotionSummaryBackButton()).getAttribute("class").contains("disabled"));

		body.getSideNavBar().getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testDocumentsRemainInBucket() {
		topNavBar.search("cow");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.searchResultCheckbox(2).click();
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount() == 2);

		topNavBar.search("bull");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount() == 2);
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 3);

		topNavBar.search("cow");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount() == 3);

		topNavBar.search("bull");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 3);

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 2);

		topNavBar.search("cow");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount() == 2);
	}

	@Test
	public void testWhitespaceSearch() {
		topNavBar.search(" ");
		assertThat("Whitespace search should not return a message as if it is a blacklisted term", !searchPage.getText().contains("All search terms are blacklisted"));
	}

	@Test
	public void testSearchParentheses() {
		topNavBar.search("(");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Bracket Mismatch in the query"));

		topNavBar.search(")");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Bracket Mismatch in the query"));

		topNavBar.search("()");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("No valid query text supplied"));

		topNavBar.search(") (");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Terminating boolean operator"));

		topNavBar.search(")war");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Bracket Mismatch in the query"));
	}

	@Test
	public void testSearchQuotationMarks() {
		topNavBar.search("\"");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Unclosed phrase"));

		topNavBar.search("\"\"");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("No valid query text supplied"));

		topNavBar.search("\" \"");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("No valid query text supplied"));

		topNavBar.search("\" \"");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("No valid query text supplied"));

		topNavBar.search("\"word");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Unclosed phrase"));

		topNavBar.search("\" word");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Unclosed phrase"));

		topNavBar.search("\" wo\"rd\"");
		assertThat("No error message shown", searchPage.getText().contains("An error occurred executing the search action"));
		assertThat("Incorrect error message shown", searchPage.getText().contains("Unclosed phrase"));
	}

	@Test
	public void testDeleteDocsFromWithinBucket() {
		topNavBar.search("sabre");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.searchResultCheckbox(2).click();
		searchPage.searchResultCheckbox(3).click();
		searchPage.searchResultCheckbox(4).click();

		final List<String> bucketList = searchPage.promotionsBucketList();
		assertThat("There should be four documents in the bucket", bucketList.size() == 4);
		assertThat("promote button not displayed when bucket has documents", searchPage.promoteTheseDocumentsButton().isDisplayed());

		for (final String bucketDocTitle : bucketList) {
			final int docIndex = bucketList.indexOf(bucketDocTitle);
			assertFalse("The document title appears as blank within the bucket for document titled " + searchPage.getSearchResult(bucketList.indexOf(bucketDocTitle) + 1).getText(), bucketDocTitle.equals(""));
			searchPage.deleteDocFromWithinBucket(bucketDocTitle);
			assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(docIndex + 1).isSelected());
			assertThat("Document not removed from bucket", !searchPage.promotionsBucketList().contains(bucketDocTitle));
			assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 3 - docIndex);
		}

		assertThat("promote button should be disabled when bucket has no documents", searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));

		topNavBar.search("tooth");
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 0);

		searchPage.searchResultCheckbox(5).click();
		final List<String> docTitles = new ArrayList<>();
		docTitles.add(searchPage.getSearchResultTitle(5));
		searchPage.javascriptClick(searchPage.forwardPageButton());
		searchPage.loadOrFadeWait();
		searchPage.searchResultCheckbox(3).click();
		docTitles.add(searchPage.getSearchResultTitle(3));

		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 2);
		assertThat("", searchPage.promotionsBucketList().containsAll(docTitles));

		searchPage.deleteDocFromWithinBucket(docTitles.get(1));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 1);
		assertThat("Document should still be in the bucket", searchPage.promotionsBucketList().contains(docTitles.get(0)));
		assertThat("Document should no longer be in the bucket", !searchPage.promotionsBucketList().contains(docTitles.get(1)));
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(3).isSelected());

		searchPage.javascriptClick(searchPage.backPageButton());
		searchPage.deleteDocFromWithinBucket(docTitles.get(0));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 0);
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(5).isSelected());
		assertThat("promote button should be disabled when bucket has no documents", searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
	}

	@Test
	public void testViewFrame() throws InterruptedException {
		topNavBar.search("army");
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));

		for (final boolean clickLogo : Arrays.asList(true, false)) {
			for (int j = 1; j <= 2; j++) {
				for (int i = 1; i <= 6; i++) {
					final String handle = getDriver().getWindowHandle();
					final String searchResultTitle = searchPage.getSearchResultTitle(i);
					searchPage.loadOrFadeWait();
					searchPage.viewFrameClick(clickLogo, i);

					Thread.sleep(5000);

					getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
					assertThat("View frame does not contain document: " + searchResultTitle, getDriver().findElement(By.xpath(".//*")).getText().contains(searchResultTitle));

					getDriver().switchTo().window(handle);
					getDriver().findElement(By.xpath("//button[contains(@id, 'cboxClose')]")).click();
					searchPage.loadOrFadeWait();
				}

				searchPage.javascriptClick(searchPage.forwardPageButton());
				searchPage.loadOrFadeWait();
			}
		}
	}

	@Test
	public void testViewFromBucketLabel() throws InterruptedException {
		topNavBar.search("جيمس");
		searchPage.selectLanguage("Arabic", getConfig().getType().getName());
		//Testing in Arabic because in some instances not latin urls have been encoded incorrectly
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		searchPage.promoteTheseDocumentsButton().click();

		for (int j = 1; j <=2; j++) {
			for (int i = 1; i <= 3; i++) {
				final String handle = getDriver().getWindowHandle();
				searchPage.searchResultCheckbox(i).click();
				final String docTitle = searchPage.getSearchResultTitle(i);
				searchPage.getPromotionBucketElementByTitle(docTitle).click();

				Thread.sleep(5000);

				getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
				assertThat("View frame does not contain document", getDriver().findElement(By.xpath(".//*")).getText().contains(docTitle));

				getDriver().switchTo().window(handle);
				getDriver().findElement(By.xpath("//button[contains(@id, 'cboxClose')]")).click();
				searchPage.loadOrFadeWait();
			}

			searchPage.javascriptClick(searchPage.forwardPageButton());
			searchPage.loadOrFadeWait();
		}
	}

	@Test
	public void testChangeLanguage() {
		String docTitle = searchPage.getSearchResultTitle(1);
		topNavBar.search("1");

		for (final String language : Arrays.asList("English", "Afrikaans", "French", "Arabic", "Urdu", "Hindi", "Chinese", "Swahili")) {
			searchPage.selectLanguage(language, getConfig().getType().getName());
			assertEquals(language, searchPage.getSelectedLanguage());

			new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
			assertNotEquals(docTitle, searchPage.getSearchResultTitle(1));

			docTitle = searchPage.getSearchResultTitle(1);
		}
	}

	@Test
	public void testBucketEmptiesWhenLanguageChangedInURL() {
		topNavBar.search("arc");
		searchPage.selectLanguage("French", getConfig().getType().getName());
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		searchPage.promoteTheseDocumentsButton().click();

		for (int i = 1; i <=4; i++) {
			searchPage.searchResultCheckbox(i).click();
		}

		assertEquals(4, searchPage.promotionsBucketWebElements().size());

		final String url = getDriver().getCurrentUrl().replace("french", "arabic");
		getDriver().get(url);
		searchPage = body.getSearchPage();
		searchPage.loadOrFadeWait();
		assertThat("Have not navigated back to search page with modified url " + url, searchPage.promoteThisQueryButton().isDisplayed());
		assertEquals(0, searchPage.promotionsBucketWebElements().size());
	}

	@Test
	public void testLanguageDisabledWhenBucketOpened() {
		//This test currently fails because language dropdown is not disabled when the promotions bucket is open
		searchPage.selectLanguage("English", getConfig().getType().getName());
		topNavBar.search("al");
		searchPage.loadOrFadeWait();
		assertThat("Languages should be enabled", !searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertEquals("There should be one document in the bucket", 1, searchPage.promotionsBucketList().size());
		searchPage.selectLanguage("French", getConfig().getType().getName());
		assertFalse("The promotions bucket should close when the language is changed", searchPage.promotionsBucket().isDisplayed());

		searchPage.promoteTheseDocumentsButton().click();
		assertEquals("There should be no documents in the bucket after changing language", 0, searchPage.promotionsBucketList().size());

		searchPage.selectLanguage("English", getConfig().getType().getName());
		assertFalse("The promotions bucket should close when the language is changed", searchPage.promotionsBucket().isDisplayed());
	}

	@Test
	public void testSearchAlternateScriptToSelectedLanguage() {
		for (final String language : Arrays.asList("French", "English", "Arabic", "Urdu", "Hindi", "Chinese")) {
			searchPage.selectLanguage(language, getConfig().getType().getName());

			for (final String script : Arrays.asList("निर्वाण", "العربية", "עברית", "сценарий", "latin", "ελληνικά", "ქართული", "བོད་ཡིག")) {
				topNavBar.search(script);
				searchPage.loadOrFadeWait();
				assertThat("Undesired error message for language: " + language + " with script: " + script, !searchPage.findElement(By.cssSelector(".search-results-view")).getText().contains("error"));
			}
		}
	}

	@Test
	public void testFieldTextFilter() {
		topNavBar.search("war");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		final String searchResultTitle = searchPage.getSearchResultTitle(1);
		final String lastWordInTitle = searchPage.getLastWord(searchResultTitle);
		int comparisonIndex = 0;
		String comparisonString = null;

		for (int i = 2; i <=6; i++) {
			if (!searchPage.getLastWord(searchPage.getSearchResultTitle(i)).equals(lastWordInTitle)) {
				comparisonIndex = i;
				comparisonString = searchPage.getSearchResultTitle(i);
				break;
			}
		}

		if (comparisonIndex == 0) {
			throw new IllegalStateException("This query is not suitable for this field text filter test");
		}

		searchPage.showFieldTextOptions();
		searchPage.fieldTextAddButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Field text input not visible", searchPage.fieldTextInput().isDisplayed());
		assertThat("Field text confirm/tick not visible", searchPage.fieldTextTickConfirm().isDisplayed());

		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("WILD{*" + lastWordInTitle + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 15).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));

		assertThat("Field text edit button not visible", searchPage.fieldTextEditButton().isDisplayed());
		assertThat("Field text remove button not visible", searchPage.fieldTextRemoveButton().isDisplayed());
		assertEquals(searchResultTitle, searchPage.getSearchResultTitle(1));

		try {
			assertNotEquals(searchPage.getSearchResultTitle(comparisonIndex), comparisonString);
		} catch (final NoSuchElementException e) {
			// The comparison document is not present
		}

		searchPage.fieldTextRemoveButton().click();
		searchPage.loadOrFadeWait();
		assertEquals(searchPage.getSearchResultTitle(comparisonIndex), comparisonString);
		assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());
		assertEquals(searchResultTitle, searchPage.getSearchResultTitle(1));
	}

	@Test
	public void testEditFieldText() {
		topNavBar.search("boer");
		searchPage.selectLanguage("Afrikaans", getConfig().getType().getName());
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		searchPage.showFieldTextOptions();
		searchPage.clearFieldText();

		final String firstSearchResult = searchPage.getSearchResultTitle(1);
		final String secondSearchResult = searchPage.getSearchResultTitle(2);

		searchPage.fieldTextAddButton().click();
		searchPage.loadOrFadeWait();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + firstSearchResult + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 15).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertEquals(firstSearchResult, searchPage.getSearchResultTitle(1));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + secondSearchResult + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 15).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertEquals(secondSearchResult, searchPage.getSearchResultTitle(1));
	}

	@Test
	public void testFieldTextInputDisappearsOnOutsideClick() {
		searchPage.showFieldTextOptions();
		assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());

		searchPage.fieldTextAddButton().click();
		assertThat("Field text add button visible", !searchPage.fieldTextAddButton().isDisplayed());
		assertThat("Field text input not visible", searchPage.fieldTextInput().isDisplayed());

		searchPage.fieldTextInput().click();
		assertThat("Field text add button visible", !searchPage.fieldTextAddButton().isDisplayed());
		assertThat("Field text input not visible", searchPage.fieldTextInput().isDisplayed());

		searchPage.showRelatedConcepts();
		assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());
		assertThat("Field text input visible", !searchPage.fieldTextInput().isDisplayed());
	}

	@Test
	public void testIdolSearchTypes() {
		topNavBar.search("leg");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		int initialSearchCount = searchPage.countSearchResults();
		topNavBar.search("leg[2:2]");
		searchPage.loadOrFadeWait();
		assertTrue(initialSearchCount > searchPage.countSearchResults());

		topNavBar.search("red");
		searchPage.loadOrFadeWait();
		initialSearchCount = searchPage.countSearchResults();
		topNavBar.search("red star");
		searchPage.loadOrFadeWait();
		final int secondSearchCount = searchPage.countSearchResults();
		assertTrue(initialSearchCount < secondSearchCount);

		topNavBar.search("\"red star\"");
		searchPage.loadOrFadeWait();
		final int thirdSearchCount = searchPage.countSearchResults();
		assertTrue(secondSearchCount > thirdSearchCount);

		topNavBar.search("red NOT star");
		searchPage.loadOrFadeWait();
		assertTrue(initialSearchCount > searchPage.countSearchResults());

		topNavBar.search("red OR star");
		searchPage.loadOrFadeWait();
		assertEquals(secondSearchCount, searchPage.countSearchResults());

		topNavBar.search("red AND star");
		searchPage.loadOrFadeWait();
		final int fourthSearchCount = searchPage.countSearchResults();
		assertTrue(secondSearchCount > fourthSearchCount);
		assertTrue(thirdSearchCount < fourthSearchCount);
	}

	@Test
	public void testFieldTextRestrictionOnPromotions(){
		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();

		topNavBar.search("darth");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		searchPage.createAMultiDocumentPromotion(2);
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat", getConfig().getType().getName());

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		assertEquals(2, searchPage.getPromotionSummarySize());
		assertEquals(2, searchPage.getPromotionSummaryLabels().size());

		final List<String> initialPromotionsSummary = searchPage.promotionsSummaryList(false);
		searchPage.showFieldTextOptions();
		searchPage.fieldTextAddButton().click();
		searchPage.fieldTextInput().sendKeys("MATCH{" + initialPromotionsSummary.get(0) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertEquals(1, searchPage.getPromotionSummarySize());
		assertEquals(1, searchPage.getPromotionSummaryLabels().size());
		assertEquals(initialPromotionsSummary.get(0), searchPage.promotionsSummaryList(false).get(0));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + initialPromotionsSummary.get(1) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertEquals(1, searchPage.getPromotionSummarySize());
		assertEquals(1, searchPage.getPromotionSummaryLabels().size());
		assertEquals(initialPromotionsSummary.get(1), searchPage.promotionsSummaryList(false).get(0));
	}

	@Test
	public void testFieldTextRestrictionOnPinToPositionPromotions(){
		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();

		topNavBar.search("horse");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		final List<String> promotedDocs = searchPage.createAMultiDocumentPromotion(2);
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.addSearchTrigger("duck");
		createPromotionsPage.finishButton().click();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		assertTrue(promotedDocs.get(0) + " should be visible", searchPage.getText().contains(promotedDocs.get(0)));
		assertTrue(promotedDocs.get(1) + " should be visible", searchPage.getText().contains(promotedDocs.get(1)));

		searchPage.showFieldTextOptions();
		searchPage.fieldTextAddButton().click();
		searchPage.fieldTextInput().sendKeys("WILD{*horse*}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertTrue(promotedDocs.get(0) + " should be visible", searchPage.getText().contains(promotedDocs.get(0)));
		assertTrue(promotedDocs.get(1) + " should be visible", searchPage.getText().contains(promotedDocs.get(1)));
		assertEquals("Wrong number of results displayed", 2, searchPage.countSearchResults());
		assertEquals("Wrong number of pin to position labels displayed", 2, searchPage.countPinToPositionLabels());

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + promotedDocs.get(0) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertEquals(promotedDocs.get(0), searchPage.getSearchResultTitle(1));
		assertEquals(1, searchPage.countSearchResults());
		assertEquals(1, searchPage.countPinToPositionLabels());

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + promotedDocs.get(1) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertEquals(promotedDocs.get(1) + " not visible in the search title", promotedDocs.get(1), searchPage.getSearchResultTitle(1));
		assertEquals("Wrong number of search results", 1, searchPage.countSearchResults());
		assertEquals("Wrong nu,ber of pin to position labels", 1, searchPage.countPinToPositionLabels());
	}

	@Test
	public void testSearchResultsCount() {
		searchPage.selectLanguage("English", getConfig().getType().getName());
		for (final String query : Arrays.asList("dog", "chips", "dinosaur", "melon", "art")) {
			topNavBar.search(query);
			searchPage.loadOrFadeWait();
			searchPage.forwardToLastPageButton().click();
			searchPage.loadOrFadeWait();
			final int numberOfPages = searchPage.getCurrentPageNumber();
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			assertEquals((numberOfPages - 1) * 6 + lastPageDocumentsCount, searchPage.countSearchResults());
		}
	}

	@Test
	public void testInvalidQueryTextNoKeywordsLinksDisplayed() {
		//TODO: map error messages to application type
		searchPage.selectLanguage("English", getConfig().getType().getName());
		for (final String searchTerm : Arrays.asList("OR", "WHEN", "SENTENCE", "SOUNDEX", "DNEAR")) {
			topNavBar.search(searchTerm);
			assertTrue("Correct error message not present for searchterm: " + searchTerm + searchPage.getText(), searchPage.getText().contains("An error occurred executing the search action"));
			assertTrue("Correct error message not present for searchterm: " + searchTerm, searchPage.getText().contains("An error occurred fetching the query analysis."));
			assertTrue("Correct error message not present for searchterm: " + searchTerm, searchPage.getText().contains("Opening boolean operator"));
		}
		for (final String searchTerm : Arrays.asList("a", "the", "of")) {
			topNavBar.search(searchTerm);
			assertTrue("Correct error message not present", searchPage.getText().contains("An error occurred executing the search action"));
			assertTrue("Correct error message not present", searchPage.getText().contains("An error occurred fetching the query analysis."));
			assertTrue("Correct error message not present", searchPage.getText().contains("No valid query text supplied"));
		}
	}

	@Test
	public void testAllowSearchOfKeywordStringsThatContainBooleansWithinThem() {
		final List<String> hiddenBooleansProximities = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "PARAGRAPHING", "NEARLY", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");
		for (final String hiddenBooleansProximity : hiddenBooleansProximities) {
			topNavBar.search(hiddenBooleansProximity);
			searchPage.loadOrFadeWait();
			assertFalse(searchPage.getText().contains("Terminating boolean operator"));
		}
	}

	@Test
	public void testFromDateFilter() throws ParseException {
		topNavBar.search("Dog");
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		final String firstResult = searchPage.getSearchResultTitle(1);
		final Date date = searchPage.getDateFromResult(1);
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.openFromDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(date);
		searchPage.closeFromDatePicker();
		assertEquals("Document should still be displayed", firstResult, searchPage.getSearchResultTitle(1));

		searchPage.openFromDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, 1));
		searchPage.closeFromDatePicker();
		assertFalse("Document should not be visible. Date filter not working", firstResult.equals(searchPage.getSearchResultTitle(1)));

		searchPage.openFromDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, -1));
		searchPage.closeFromDatePicker();
		assertTrue("Document should be visible. Date filter not working", firstResult.equals(searchPage.getSearchResultTitle(1)));
	}

	@Test
	public void testUntilDateFilter() throws ParseException {
		topNavBar.search("Dog");
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		final String firstResult = searchPage.getSearchResultTitle(1);
		final Date date = searchPage.getDateFromResult(1);
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(date);
		searchPage.closeUntilDatePicker();
		assertEquals("Document should still be displayed", firstResult, searchPage.getSearchResultTitle(1));

		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, -1));
		searchPage.closeUntilDatePicker();
		assertFalse("Document should not be visible. Date filter not working", firstResult.equals(searchPage.getSearchResultTitle(1)));

		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, 1));
		searchPage.closeUntilDatePicker();
		assertTrue("Document should be visible. Date filter not working", firstResult.equals(searchPage.getSearchResultTitle(1)));
	}

	@Test
	public void testFromDateAlwaysBeforeUntilDate() {
		topNavBar.search("food");
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:00");
		searchPage.fromDateTextBox().sendKeys(Keys.ENTER);
		searchPage.untilDateTextBox().sendKeys("04/05/2000 12:00");
		assertEquals("Dates should be equal", searchPage.fromDateTextBox().getAttribute("value"), searchPage.untilDateTextBox().getAttribute("value"));

		searchPage.fromDateTextBox().clear();
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:01");
		searchPage.fromDateTextBox().sendKeys(Keys.ENTER);
		assertFalse("From date cannot be after the until date", searchPage.fromDateTextBox().getAttribute("value").equals("04/05/2000 12:01"));

		searchPage.fromDateTextBox().clear();
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:00");
		searchPage.untilDateTextBox().clear();
		searchPage.untilDateTextBox().sendKeys("04/05/2000 11:59");
		searchPage.untilDateTextBox().sendKeys(Keys.ENTER);
		assertFalse("Until date cannot be before the from date", searchPage.untilDateTextBox().getAttribute("value").equals("04/05/2000 11:59"));
	}

	@Test
	public void testSortByRelevance() {
		topNavBar.search("string");
		searchPage.sortByRelevance();
		List<Float> weights = searchPage.getWeightsOnPage(5);
		for (int i = 0; i < weights.size() - 1; i++) {
			assertTrue("Weight of search result " + i + " is not greater that weight of search result " + (i+1), weights.get(i) >= weights.get(i + 1));
		}

		searchPage.sortByDate();
		searchPage.sortByRelevance();
		weights = searchPage.getWeightsOnPage(5);
		for (int i = 0; i < weights.size() - 1; i++) {
			assertTrue("Weight of search result " + i + " is not greater that weight of search result " + (i+1), weights.get(i) >= weights.get(i + 1));
		}

		searchPage.sortByDate();
		topNavBar.search("paper packages");
		searchPage.sortByRelevance();
		weights = searchPage.getWeightsOnPage(5);
		for (int i = 0; i < weights.size() - 1; i++) {
			assertTrue("Weight of search result " + i + " is not greater that weight of search result " + (i+1), weights.get(i) >= weights.get(i + 1));
		}
	}

	@Test
	public void testSearchBarTextPersistsOnRefresh() {
		final String searchText = "Stay";
		topNavBar.search(searchText);

		// Change to promotions page since the search page will persist the query in the URL
		navBar.switchPage(NavBarTabId.PROMOTIONS);

		getDriver().navigate().refresh();
		final String newSearchText = new TopNavBar(getDriver()).getSearchBarText();
		assertTrue("search bar should be blank on refresh of a page that isn't the search page", newSearchText.equals(searchText));
	}

	@Test
	public void testRelatedConceptsLinks() {
		String queryText = "frog";
		topNavBar.search(queryText);
		assertTrue("The search bar has not retained the query text", topNavBar.getSearchBarText().equals(queryText));
		assertTrue("'You searched for' section does not include query text", searchPage.youSearchedFor().contains(queryText));
		assertTrue("'Results for' heading text does not contain the query text", searchPage.getResultsForText().contains(queryText));

		for (int i = 0; i < 5; i++) {
			searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
			searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
			final int conceptsCount = searchPage.countRelatedConcepts();
			assertTrue("Number of related concepts exceeds 50", conceptsCount <= 50);
			final int index = new Random().nextInt(conceptsCount);
			queryText = searchPage.getRelatedConcepts().get(index).getText();
			searchPage.relatedConcept(queryText).click();
			searchPage.waitForSearchLoadIndicatorToDisappear();

			assertTrue("The search bar has not retained the query text", topNavBar.getSearchBarText().equals(queryText));
			final String[] words = queryText.split("\\s+");
			for (final String word : words) {
				assertTrue("'You searched for' section does not include word: " + word + " for query text: " + queryText, searchPage.youSearchedFor().contains(word));
			}
			assertTrue("'Results for' heading text does not contain the query text: " + queryText, searchPage.getResultsForText().contains(queryText));
		}
	}

	@Test
	public void testRelatedConceptsDifferentInDifferentLanguages() {
		topNavBar.search("France");
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> englishConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		searchPage.selectLanguage("French", getConfig().getType().getName());
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> frenchConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		assertFalse("Concepts should be different in different languages", englishConcepts.equals(frenchConcepts));

		searchPage.selectLanguage("English", getConfig().getType().getName());
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> secondEnglishConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		assertEquals("Related concepts have changed on second search of same query text", englishConcepts, secondEnglishConcepts);
	}

	@Test
	public void testNavigateToLastPageOfSearchResultsAndEditUrlToTryAndNavigateFurther() {
		topNavBar.search("nice");
		searchPage.forwardToLastPageButton().click();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		final int currentPage = searchPage.getCurrentPageNumber();
		final String docTitle = searchPage.getSearchResultTitle(4);
		final String url = getDriver().getCurrentUrl();
		assertTrue("Url and current page number are out of sync", url.contains("nice/" + currentPage));
		final String illegitimateUrl = url.replace("nice/" + currentPage, "nice/" + (currentPage + 5));
		getDriver().navigate().to(illegitimateUrl);
		searchPage = new AppBody(getDriver()).getSearchPage();
		assertEquals("Page number should not have changed", currentPage, searchPage.getCurrentPageNumber());
		assertEquals("Url should have reverted to original url", url, getDriver().getCurrentUrl());
		assertFalse("Error message should not be showing", searchPage.isErrorMessageShowing());
		assertEquals("Search results have changed on last page", docTitle, searchPage.getSearchResultTitle(4));
	}

	@Test
	public void testNoRelatedConceptsIfNoResultsFound() {
		final String garbageQueryText = "garbagedjlsfjijlsf";
		topNavBar.search(garbageQueryText);
		assertTrue("Garbage text returned results. garbageQueryText string needs changed to be more garbage like", searchPage.getText().contains("No results found"));
		assertEquals("Garbage text returned results. garbageQueryText string needs changed to be more garbage like", 0, searchPage.countSearchResults());

		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		assertTrue("If there are no search results there should be no related concepts", searchPage.getText().contains("No related concepts found"));
	}

	@Test
	public void testParametricValuesLoads() throws InterruptedException {
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.PARAMETRIC_VALUES);
		Thread.sleep(20000);
		assertFalse("Load indicator still visible after 20 seconds", searchPage.parametricValueLoadIndicator().isDisplayed());
	}
}
