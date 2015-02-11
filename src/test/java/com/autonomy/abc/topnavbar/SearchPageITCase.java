package com.autonomy.abc.topnavbar;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SearchPageITCase extends ABCTestBase {
	public SearchPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;
	private CreateNewPromotionsPage createPromotionsPage;
	private PromotionsPage promotionsPage;

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
		assertThat("Search title text is wrong", searchPage.searchTitle().getText().equals("dog"));

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

		searchPage.forwardPageButton().click();
		searchPage.paginateWait();
		assertThat("Back to first page button is not enabled", !AppElement.getParent(searchPage.backToFirstPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Back a page button is not enabled", !AppElement.getParent(searchPage.backPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Page 2 is not active", searchPage.isPageActive(2));

		searchPage.forwardPageButton().click();
		searchPage.paginateWait();
		searchPage.forwardPageButton().click();
		searchPage.paginateWait();
		searchPage.backPageButton().click();
		searchPage.paginateWait();
		assertThat("Page 3 is not active", searchPage.isPageActive(3));

		searchPage.backToFirstPageButton().click();
		searchPage.paginateWait();
		assertThat("Page 1 is not active", searchPage.isPageActive(1));

		searchPage.forwardToLastPageButton().click();
		searchPage.paginateWait();
		assertThat("Forward to last page button is not disabled", AppElement.getParent(searchPage.forwardToLastPageButton()).getAttribute("class").contains("disabled"));
		assertThat("Forward a page button is not disabled", AppElement.getParent(searchPage.forwardPageButton()).getAttribute("class").contains("disabled"));

		final int numberOfPages = searchPage.getCurrentPageNumber();

		for (int i = numberOfPages - 1; i > 0; i--) {
			searchPage.backPageButton().click();
			searchPage.paginateWait();
			assertThat("Page " + String.valueOf(i) + " is not active", searchPage.isPageActive(i));
			assertThat("Url incorrect", getDriver().getCurrentUrl().endsWith(String.valueOf(i)));
		}

		for (int j = 2; j < numberOfPages + 1; j++) {
			searchPage.forwardPageButton().click();
			searchPage.paginateWait();
			assertThat("Page " + String.valueOf(j) + " is not active", searchPage.isPageActive(j));
			assertThat("Url incorrect", getDriver().getCurrentUrl().endsWith(String.valueOf(j)));
		}
	}

	// This used to fail because the predict=false parameter was not added to our query actions
	@Test
	public void testPaginationAndBackButton() {
		topNavBar.search("sith");
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
		topNavBar.search("sail");
		searchPage.createAMultiDocumentPromotion(18);
		createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat");

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("boat").click();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		promotionsPage.clickableSearchTrigger("boat").click();
		promotionsPage.loadOrFadeWait();
		assertThat("Summary size should equal 2", searchPage.getPromotionSummarySize() == 2);

		searchPage.showMore();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 5);

		searchPage.showLessButton();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize() == 2);

		searchPage.showMore();
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
			searchPage.deleteDocFromWithinBucket(bucketDocTitle);
			assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(docIndex + 1).isSelected());
			assertThat("Document not removed from bucket", !searchPage.promotionsBucketList().contains("bucketDocTitle"));
			assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 3 - docIndex);
		}

		assertThat("promote button should not be displayed when bucket has no documents", !searchPage.promoteTheseItemsButton().isDisplayed());

		topNavBar.search("tooth");
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 0);

		searchPage.searchResultCheckbox(5).click();
		final List<String> docTitles = new ArrayList<>();
		docTitles.add(searchPage.getSearchResultTitle(5));
		searchPage.forwardPageButton().click();
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

		searchPage.backPageButton().click();
		searchPage.deleteDocFromWithinBucket(docTitles.get(0));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size() == 0);
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(5).isSelected());
		assertThat("promote button should not be displayed when bucket has no documents", !searchPage.promoteTheseItemsButton().isDisplayed());
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
					assertThat("View frame does not contain document", getDriver().findElement(By.xpath(".//*")).getText().contains(searchResultTitle));

					getDriver().switchTo().window(handle);
					getDriver().findElement(By.xpath("//button[contains(@id, 'cboxClose')]")).click();
					searchPage.loadOrFadeWait();
				}

				searchPage.forwardPageButton().click();
				searchPage.loadOrFadeWait();
			}
		}
	}

	@Test
	public void testViewFromBucketLabel() throws InterruptedException {
		topNavBar.search("جيمس");
		searchPage.selectLanguage("Arabic");
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

			searchPage.forwardPageButton().click();
			searchPage.loadOrFadeWait();
		}
	}

	@Test
	public void testChangeLanguage() {
		String docTitle = searchPage.getSearchResultTitle(1);
		topNavBar.search("1");

		for (final String language : Arrays.asList("English", "Afrikaans", "French", "Arabic", "Urdu", "Hindi", "Chinese", "Swahili")) {
			searchPage.selectLanguage(language);
			assertEquals(language, searchPage.getSelectedLanguage());

			new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
			assertNotEquals(docTitle, searchPage.getSearchResultTitle(1));

			docTitle = searchPage.getSearchResultTitle(1);
		}
	}

	@Test
	public void testBucketEmptiesWhenLanguageChangedInURL() {
		topNavBar.search("arc");
		searchPage.selectLanguage("French");
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
		searchPage.selectLanguage("Hindi");
		topNavBar.search("पपीहा");
		searchPage.loadOrFadeWait();
		assertThat("Languages should be enabled", !searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.promoteTheseDocumentsButton().click();
		assertThat("Languages should be disabled", searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.promotionsBucketClose();
		assertThat("Languages should be enabled", !searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.selectLanguage("Arabic");
		searchPage.promoteTheseDocumentsButton().click();
		assertThat("Languages should be disabled", searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.promotionsBucketClose();
		assertThat("Languages should be enabled", !searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));
	}

	@Test
	public void testSearchAlternateScriptToSelectedLanguage() {
		for (final String language : Arrays.asList("French", "English", "Arabic", "Urdu", "Hindi", "Chinese")) {
			searchPage.selectLanguage(language);

			for (final String script : Arrays.asList("निर्वाण", "العربية", "עברית", "сценарий", "latin", "ελληνικά", "ქართული", "བོད་ཡིག")) {
				topNavBar.search(script);
				searchPage.loadOrFadeWait();
				assertThat("Undesired error message", !searchPage.findElement(By.cssSelector(".search-results-view")).getText().contains("error"));
			}
		}
	}

	@Test
	public void testDatabaseSelection() {
		topNavBar.search("car");
		searchPage.selectLanguage("English");
		searchPage.selectDatabase("All");
		assertThat("All databases not showing", searchPage.getSelectedDatabases().contains("All"));

		searchPage.selectDatabase("WikiEnglish");
		assertThat("Database not showing", searchPage.getSelectedDatabases().contains("WikiEnglish"));
		final String wikiEnglishResult = searchPage.getSearchResult(1).getText();
		searchPage.deselectDatabase("WikiEnglish");

		searchPage.selectDatabase("Wookiepedia");
		assertThat("Database not showing", searchPage.getSelectedDatabases().contains("Wookiepedia"));
		final String wookiepediaResult = searchPage.getSearchResult(1).getText();
		assertNotEquals(wookiepediaResult, wikiEnglishResult);

		searchPage.selectDatabase("WikiEnglish");
		assertThat("Databases not showing", searchPage.getSelectedDatabases().containsAll(Arrays.asList("Wookiepedia", "WikiEnglish")));
		assertThat("Result not from selected databases", searchPage.getSearchResult(1).getText().equals(wookiepediaResult) || searchPage.getSearchResult(1).getText().equals(wikiEnglishResult));
	}

	@Test
	public void testFieldTextFilter() {
		topNavBar.search("war");
		searchPage.selectLanguage("English");
		searchPage.selectDatabase("All");
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
}
