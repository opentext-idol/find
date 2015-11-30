package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchFilter;
import com.autonomy.abc.selenium.util.Errors;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPageITCase extends ABCTestBase {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private SearchPage searchPage;
	private TopNavBar topNavBar;
	private CreateNewPromotionsPage createPromotionsPage;
	private PromotionsPage promotionsPage;
	DatePicker datePicker;
	String havenErrorMessage = "Haven OnDemand returned an error while executing the search action";

	public SearchPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();
	}

	//TODO move this to SearchBase (and refactor code)
    private void selectNewsEngIndex() {
		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()[contains(.,'news_eng')]]"))).click();
		}
    }

	private void search(String searchTerm){
		logger.info("Searching for: '" + searchTerm + "'");
		topNavBar.search(searchTerm);
		searchPage.waitForSearchLoadIndicatorToDisappear();
		assertThat(searchPage, not(containsText(Errors.Search.HOD)));
	}

	private void applyFilter(SearchFilter filter) {
		filter.apply(searchPage);
		searchPage.waitForSearchLoadIndicatorToDisappear();
	}

    @Test
	public void testUnmodifiedResultsToggleButton(){
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

        assertThat("Page should be showing modified results", searchPage.modifiedResultsShown(), is(true));
		assertThat("Url incorrect", getDriver().getCurrentUrl(), containsString("/modified"));

		searchPage.modifiedResultsCheckBox().click();
        assertThat("Page should not be showing modified results", searchPage.modifiedResultsShown(), is(false));
		assertThat("Url incorrect", getDriver().getCurrentUrl(), containsString("/unmodified"));

		searchPage.modifiedResultsCheckBox().click();
        assertThat("Page should be showing modified results", searchPage.modifiedResultsShown(), is(true));
        assertThat("Url incorrect", getDriver().getCurrentUrl(), containsString("/modified"));
	}

	@Test
	public void testSearchBasic(){
		search("dog");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText(), is("dog"));

		search("cat");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText(), is("cat"));

		search("ElEPhanT");
		assertThat("Search title text is wrong", searchPage.searchTitle().getText(),is("ElEPhanT"));
	}

	@Test
	public void testPromoteButton(){
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be enabled", searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount(), is(0));

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promote these items button should be enabled", !searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(1));

		searchPage.promotionsBucketClose();
		assertThat("Promoted items bucket has not appeared", searchPage.getText(), not(containsString("Select Items to Promote")));

		searchPage.promoteTheseDocumentsButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be enabled", searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount(),is(0));
	}

	@Test
	public void testAddFilesToPromoteBucket() {
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.loadOrFadeWait();

		for (int i = 1; i < 7; i++) {
			AppElement.scrollIntoView(searchPage.searchResultCheckbox(i), getDriver());
			searchPage.searchResultCheckbox(i).click();
			assertThat("Promoted items count not correct", searchPage.promotedItemsCount(),is(i));
		}

		for (int j = 6; j > 0; j--) {
			AppElement.scrollIntoView(searchPage.searchResultCheckbox(j), getDriver());
			searchPage.searchResultCheckbox(j).click();
			assertThat("Promoted items count not correct", searchPage.promotedItemsCount(), is(j - 1));
		}

		searchPage.promotionsBucketClose();
	}

    //TODO fix this test so it's not being run on something with an obscene amount of pages
    @Ignore
	@Test
	public void testSearchResultsPagination() {
		search("dog");
		searchPage.loadOrFadeWait();
		assertThat("Back to first page button is not disabled", searchPage.isBackToFirstPageButtonDisabled());
		assertThat("Back a page button is not disabled", AppElement.getParent(searchPage.backPageButton()).getAttribute("class"),containsString("disabled"));

		searchPage.javascriptClick(searchPage.forwardPageButton());
		searchPage.paginateWait();
 		assertThat("Back to first page button is not enabled", AppElement.getParent(searchPage.backToFirstPageButton()).getAttribute("class"),not(containsString("disabled")));
		assertThat("Back a page button is not enabled", AppElement.getParent(searchPage.backPageButton()).getAttribute("class"),not(containsString("disabled")));
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
		assertThat("Forward to last page button is not disabled", AppElement.getParent(searchPage.forwardToLastPageButton()).getAttribute("class"),containsString("disabled"));
		assertThat("Forward a page button is not disabled", AppElement.getParent(searchPage.forwardPageButton()).getAttribute("class"),containsString("disabled"));

		final int numberOfPages = searchPage.getCurrentPageNumber();

		for (int i = numberOfPages - 1; i > 0; i--) {
			searchPage.javascriptClick(searchPage.backPageButton());
			searchPage.paginateWait();
			assertThat("Page " + String.valueOf(i) + " is not active", searchPage.isPageActive(i));
			assertThat("Url incorrect", getDriver().getCurrentUrl(),endsWith(String.valueOf(i)));
		}

		for (int j = 2; j < numberOfPages + 1; j++) {
			searchPage.javascriptClick(searchPage.forwardPageButton());
			searchPage.paginateWait();
			assertThat("Page " + String.valueOf(j) + " is not active", searchPage.isPageActive(j));
			assertThat("Url incorrect", getDriver().getCurrentUrl(),endsWith(String.valueOf(j)));
		}
	}

	// This used to fail because the predict=false parameter was not added to our query actions
	@Test
	public void testPaginationAndBackButton() {
		search("safe");
		searchPage.forwardToLastPageButton().click();
		searchPage.loadOrFadeWait();
		assertThat("Forward to last page button is not disabled", searchPage.forwardToLastPageButton().getAttribute("class"),containsString("disabled"));
		assertThat("Forward a page button is not disabled", searchPage.forwardPageButton().getAttribute("class"),containsString("disabled"));
		final int lastPage = searchPage.getCurrentPageNumber();

		getDriver().navigate().back();
		assertThat("Back button has not brought the user back to the first page", searchPage.getCurrentPageNumber(),is(1));

		getDriver().navigate().forward();
		assertThat("Forward button has not brought the user back to the last page", searchPage.getCurrentPageNumber(), is(lastPage));
	}

	@Test
	public void testAddDocumentToPromotionsBucket() {
		search("horse");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(1));
		assertThat("File in bucket description does not match file added", searchPage.getSearchResultTitle(1), equalToIgnoringCase(searchPage.bucketDocumentTitle(1)));
	}

	@Test
	public void testPromoteTheseItemsButtonLink() {
		search("fox");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.promoteTheseItemsButton().click();
		assertThat("Create new promotions page not open", getDriver().getCurrentUrl(), endsWith("promotions/create"));
	}

	@Test
	public void testMultiDocPromotionDrawerExpandAndPagination() {
		search("freeze");
        searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.createAMultiDocumentPromotion(18);
		createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat");

		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("boat").click();
		PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        assertThat(promotionsDetailPage.getText(), containsString("Trigger terms"));
 		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(promotionsDetailPage.triggerAddButton()));

		promotionsDetailPage.trigger("boat").click();
		promotionsDetailPage.loadOrFadeWait();

        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
        searchPage.loadOrFadeWait();

		assertThat("Summary size should equal 2", searchPage.getPromotionSummarySize(), is(2));

		assertThat("The show more promotions button is not visible", searchPage.showMorePromotionsButton(), displayed());
		searchPage.showMorePromotions();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize(), is(5));

		searchPage.showLessPromotions();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize(), is(2));

		searchPage.showMorePromotions();
		assertThat("Summary size should equal 5", searchPage.getPromotionSummarySize(), is(5));

		assertThat("Back to start button should be disabled", searchPage.promotionSummaryBackToStartButton().getAttribute("class"),containsString("disabled"));
		assertThat("Back button should be disabled", searchPage.promotionSummaryBackButton().getAttribute("class"),containsString("disabled"));
		assertThat("Forward button should be enabled", searchPage.promotionSummaryForwardButton().getAttribute("class"), not(containsString("disabled")));
		assertThat("Forward to end button should be enabled", searchPage.promotionSummaryForwardToEndButton().getAttribute("class"), not(containsString("disabled")));

		searchPage.promotionSummaryForwardButton().click();
		searchPage.waitForPromotionsLoadIndicatorToDisappear();
		assertThat("Back to start button should be enabled", searchPage.promotionSummaryBackToStartButton().getAttribute("class"), not(containsString("disabled")));
		assertThat("Back button should be enabled", searchPage.promotionSummaryBackButton().getAttribute("class"),not(containsString("disabled")));
		assertThat("Forward button should be enabled", searchPage.promotionSummaryForwardButton().getAttribute("class"), not(containsString("disabled")));
		assertThat("Forward to end button should be enabled", searchPage.promotionSummaryForwardToEndButton().getAttribute("class"), not(containsString("disabled")));

		searchPage.promotionSummaryForwardButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		searchPage.promotionSummaryForwardButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		assertThat("Back to start button should be enabled", searchPage.promotionSummaryBackToStartButton().getAttribute("class"),not(containsString("disabled")));
		assertThat("Back button should be enabled", searchPage.promotionSummaryBackButton().getAttribute("class"), not(containsString("disabled")));
		assertThat("Forward button should be disabled", searchPage.promotionSummaryForwardButton().getAttribute("class"), containsString("disabled"));
        assertThat("Forward to end button should be disabled", searchPage.promotionSummaryForwardToEndButton().getAttribute("class"), containsString("disabled"));

		searchPage.promotionSummaryBackButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		searchPage.promotionSummaryBackButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		searchPage.promotionSummaryBackButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		assertThat("Back to start button should be disabled", searchPage.promotionSummaryBackToStartButton().getAttribute("class"), containsString("disabled"));

		searchPage.promotionSummaryForwardToEndButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		assertThat("Forward to end button should be disabled", searchPage.promotionSummaryForwardToEndButton().getAttribute("class"), containsString("disabled"));

		searchPage.promotionSummaryBackToStartButton().click();
        searchPage.waitForPromotionsLoadIndicatorToDisappear();
		assertThat("Back button should be disabled", searchPage.promotionSummaryBackButton().getAttribute("class"),containsString("disabled"));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testDocumentsRemainInBucket() {
		search("cow");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.searchResultCheckbox(2).click();
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount(), is(2));

		search("bull");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount(), is(2));
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(3));

		search("cow");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount(), is(3));

		search("bull");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(3));

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(2));

		search("cow");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(2));
	}

	@Test
	public void testWhitespaceSearch() {
		search(" ");
		assertThat("Whitespace search should not return a message as if it is a blacklisted term",
                searchPage.getText(),not(containsString("All search terms are blacklisted")));
	}

    String searchErrorMessage = "An error occurred executing the search action";
    String correctErrorMessageNotShown = "Correct error message not shown";

	@Test
	public void testSearchParentheses() {
        List<String> testSearchTerms = Arrays.asList("(",")","()",") (",")war");

        if(getConfig().getType().equals(ApplicationType.HOSTED)){
            for(String searchTerm : testSearchTerms){
                search(searchTerm);

                assertThat(searchPage.getText(),containsString(Errors.Search.HOD));
            }
        } else if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
            int searchTerm = 0;
            String bracketMismatch = "Bracket Mismatch in the query";
            search(testSearchTerms.get(searchTerm++));

            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(bracketMismatch));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(bracketMismatch));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString("No valid query text supplied"));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString("Terminating boolean operator"));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(bracketMismatch));
        } else {
            fail("Config type not recognised");
        }
	}

    //TODO there are some which contain helpful error messages?
	@Test
	public void testSearchQuotationMarks() {
        List<String> testSearchTerms = Arrays.asList("\"","\"\"","\" \"","\" \"","\"word","\" word","\" wo\"rd\"");

        if(getConfig().getType().equals(ApplicationType.HOSTED)){
            for (String searchTerm : testSearchTerms){
                search(searchTerm);
                assertThat(searchPage.getText(),containsString(Errors.Search.HOD));
            }
        } else if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
            String noValidQueryText = "No valid query text supplied";
            String unclosedPhrase = "Unclosed phrase";
            int searchTerm = 0;

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(unclosedPhrase));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(noValidQueryText));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(noValidQueryText));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(noValidQueryText));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(unclosedPhrase));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString("An error occurred executing the search action"));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(unclosedPhrase));

            search(testSearchTerms.get(searchTerm++));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString("An error occurred executing the search action"));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(unclosedPhrase));
        } else {
            fail("Config type not recognised");
        }
	}

	@Test
	public void testDeleteDocsFromWithinBucket() {
		search("sabre");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		searchPage.searchResultCheckbox(2).click();
		searchPage.searchResultCheckbox(3).click();
		searchPage.searchResultCheckbox(4).click();

		final List<String> bucketList = searchPage.promotionsBucketList();
		final List<WebElement> bucketListElements = searchPage.promotionsBucketWebElements();
		assertThat("There should be four documents in the bucket", bucketList.size(),is(4));
		assertThat("promote button not displayed when bucket has documents", searchPage.promoteTheseDocumentsButton().isDisplayed());

//		for (final String bucketDocTitle : bucketList) {
//			final int docIndex = bucketList.indexOf(bucketDocTitle);
//			assertFalse("The document title appears as blank within the bucket for document titled " + searchPage.getSearchResult(bucketList.indexOf(bucketDocTitle) + 1).getText(), bucketDocTitle.equals(""));
//			searchPage.deleteDocFromWithinBucket(bucketDocTitle);
//			assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(docIndex + 1).isSelected());
//			assertThat("Document not removed from bucket", searchPage.promotionsBucketList(),not(hasItem(bucketDocTitle)));
//			assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size(),is(3 - docIndex));
//		}

		for (final WebElement bucketDoc : bucketListElements) {
			bucketDoc.findElement(By.cssSelector("i:nth-child(2)")).click();
		}

		assertThat("promote button should be disabled when bucket has no documents", searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));

		search("tooth");
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size(), is(0));

		searchPage.searchResultCheckbox(5).click();
		final List<String> docTitles = new ArrayList<>();
		docTitles.add(searchPage.getSearchResultTitle(5));
		searchPage.javascriptClick(searchPage.forwardPageButton());
		searchPage.loadOrFadeWait();
		searchPage.searchResultCheckbox(3).click();
		docTitles.add(searchPage.getSearchResultTitle(3));

		final List<String> bucketListNew = searchPage.promotionsBucketList();
		assertThat("Wrong number of documents in the bucket", bucketListNew.size(),is(2));
//		assertThat("", searchPage.promotionsBucketList().containsAll(docTitles));
		assertThat(bucketListNew.size(),is(docTitles.size()));

		for(String docTitle : docTitles){
			assertThat(bucketListNew,hasItem(docTitle.toUpperCase()));
		}

		searchPage.deleteDocFromWithinBucket(docTitles.get(1));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size(),is(1));
		assertThat("Document should still be in the bucket", searchPage.promotionsBucketList(),hasItem(docTitles.get(0).toUpperCase()));
		assertThat("Document should no longer be in the bucket", searchPage.promotionsBucketList(),not(hasItem(docTitles.get(1).toUpperCase())));
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(3).isSelected());

		searchPage.javascriptClick(searchPage.backPageButton());
		searchPage.deleteDocFromWithinBucket(docTitles.get(0));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList().size(),is(0));
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(5).isSelected());
		assertThat("promote button should be disabled when bucket has no documents", searchPage.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
	}

	@Test
	public void testViewFrame() throws InterruptedException {
		search("army");
		searchPage.waitForSearchLoadIndicatorToDisappear();

		for (final boolean clickLogo : Arrays.asList(true, false)) {
			for (int j = 1; j <= 2; j++) {
				for (int i = 1; i <= 6; i++) {
					final String handle = getDriver().getWindowHandle();
					final String searchResultTitle = searchPage.getSearchResultTitle(i);
					searchPage.loadOrFadeWait();
					searchPage.viewFrameClick(clickLogo, i);

					Thread.sleep(5000);

					getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
					assertThat("View frame does not contain document: " + searchResultTitle, getDriver().findElement(By.xpath(".//*")).getText(),
                            containsString(searchResultTitle));

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
        search("جيمس");
		searchPage.selectLanguage(Language.ARABIC);
        logger.warn("Using Trimmed Titles");

        search("Engineer");

		//Testing in Arabic because in some instances not latin urls have been encoded incorrectly
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.promoteTheseDocumentsButton().click();

		for (int j = 1; j <=2; j++) {
			for (int i = 1; i <= 3; i++) {
				final String handle = getDriver().getWindowHandle();
				searchPage.searchResultCheckbox(i).click();
				final String docTitle = searchPage.getSearchResultTitle(i);
				searchPage.getPromotionBucketElementByTitle(docTitle).click();

				Thread.sleep(5000);

				getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
                //Using trimmedtitle is a really hacky way to get around the latin urls not being encoded (possibly, or another problem) correctly
				assertThat("View frame does not contain document", getDriver().findElement(By.xpath(".//*")).getText(),containsString(docTitle));

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
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), Matchers.not(ApplicationType.HOSTED));
		String docTitle = searchPage.getSearchResultTitle(1);
		search("1");

		List<Language> languages = Arrays.asList(Language.ENGLISH, Language.AFRIKAANS, Language.FRENCH, Language.ARABIC, Language.URDU, Language.HINDI, Language.CHINESE, Language.SWAHILI);
		for (final Language language : languages) {
			searchPage.selectLanguage(language);
			assertThat(searchPage.getSelectedLanguage(), is(language.toString()));

			searchPage.waitForSearchLoadIndicatorToDisappear();
			assertThat(searchPage.getSearchResultTitle(1), not(docTitle));

			docTitle = searchPage.getSearchResultTitle(1);
		}
	}

	@Test
	public void testBucketEmptiesWhenLanguageChangedInURL() {
		search("arc");
		searchPage.selectLanguage(Language.FRENCH);
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.promoteTheseDocumentsButton().click();

		for (int i = 1; i <=4; i++) {
			searchPage.searchResultCheckbox(i).click();
		}

		assertThat(searchPage.promotionsBucketWebElements(), hasSize(4));

		final String url = getDriver().getCurrentUrl().replace("french", "arabic");
		getDriver().get(url);
		searchPage = getElementFactory().getSearchPage();
		searchPage.loadOrFadeWait();
		assertThat("Have not navigated back to search page with modified url " + url, searchPage.promoteThisQueryButton().isDisplayed());
		assertThat(searchPage.promotionsBucketWebElements(), hasSize(0));
	}

	@Test
	public void testLanguageDisabledWhenBucketOpened() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), Matchers.not(ApplicationType.HOSTED));
		//This test currently fails because language dropdown is not disabled when the promotions bucket is open
		searchPage.selectLanguage(Language.ENGLISH);
		search("al");
		searchPage.loadOrFadeWait();
		assertThat("Languages should be enabled", !searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertThat("There should be one document in the bucket", searchPage.promotionsBucketList(), hasSize(1));
		searchPage.selectLanguage(Language.FRENCH);
		assertThat("The promotions bucket should close when the language is changed", searchPage.promotionsBucket(), not(displayed()));

		searchPage.promoteTheseDocumentsButton().click();
		assertThat("There should be no documents in the bucket after changing language", searchPage.promotionsBucketList(), hasSize(0));

		searchPage.selectLanguage(Language.ENGLISH);
		assertThat("The promotions bucket should close when the language is changed", searchPage.promotionsBucket(), not(displayed()));
	}

	@Test
	public void testSearchAlternateScriptToSelectedLanguage() {
		List<Language> languages = Arrays.asList(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.URDU, Language.HINDI, Language.CHINESE);
		for (final Language language : languages) {
			searchPage.selectLanguage(language);

			for (final String script : Arrays.asList("निर्वाण", "العربية", "עברית", "сценарий", "latin", "ελληνικά", "ქართული", "བོད་ཡིག")) {
				search(script);
				searchPage.loadOrFadeWait();
				assertThat("Undesired error message for language: " + language + " with script: " + script, searchPage.findElement(By.cssSelector(".search-results-view")).getText(),not(containsString("error")));
			}
		}
	}

	@Test
	public void testFieldTextFilter() {
		if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
			search("war");
			searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		} else {
			new Search(getApplication(), getElementFactory(), "*").applyFilter(new IndexFilter("sitesearch")).apply();
		}
		
		searchPage.selectLanguage(Language.ENGLISH);

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
		searchPage.waitForSearchLoadIndicatorToDisappear();
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
		searchPage.waitForSearchLoadIndicatorToDisappear();

		assertThat("Field text edit button not visible", searchPage.fieldTextEditButton().isDisplayed());
		assertThat("Field text remove button not visible", searchPage.fieldTextRemoveButton().isDisplayed());
		assertThat(searchPage.getSearchResultTitle(1), is(searchResultTitle));

		try {
			assertThat(searchPage.getSearchResultTitle(comparisonIndex), not(comparisonString));
		} catch (final NoSuchElementException e) {
			// The comparison document is not present
		}

		searchPage.fieldTextRemoveButton().click();
		searchPage.loadOrFadeWait();
		assertThat(searchPage.getSearchResultTitle(comparisonIndex), is(comparisonString));
		assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());
		assertThat(searchPage.getSearchResultTitle(1), is(searchResultTitle));
	}

	@Test
	public void testEditFieldText() {
		if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
			searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
			search("boer");
		} else {
			new Search(getApplication(), getElementFactory(), "*").applyFilter(new IndexFilter("sitesearch")).apply();
		}

		searchPage.selectLanguage(Language.AFRIKAANS);


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
		searchPage.waitForSearchLoadIndicatorToDisappear();
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
		assertThat(searchPage.getText(), not(containsString("No results found")));
		assertThat(searchPage.getSearchResultTitle(1), is(firstSearchResult));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + secondSearchResult + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
		assertThat(searchPage.getSearchResultTitle(1), is(secondSearchResult));
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

    //TODO
	@Test
	public void testIdolSearchTypes() {
		final int redCount = getResultCount("red");
		final int starCount = getResultCount("star");
		final int unquotedCount = getResultCount("red star");
		final int quotedCount = getResultCount("\"red star\"");
		final int orCount = getResultCount("red OR star");
		final int andCount = getResultCount("red AND star");
		final int redNotStarCount = getResultCount("red NOT star");
		final int starNotRedCount = getResultCount("star NOT red");

		verifyThat(redCount, lessThanOrEqualTo(unquotedCount));
        verifyThat(quotedCount, lessThanOrEqualTo(unquotedCount));

		verifyThat(redNotStarCount, lessThanOrEqualTo(redCount));
		verifyThat(starNotRedCount, lessThanOrEqualTo(starCount));

		verifyThat(quotedCount, lessThanOrEqualTo(andCount));
		verifyThat(andCount, lessThanOrEqualTo(unquotedCount));

		verifyThat(orCount, lessThanOrEqualTo(redCount + starCount));
		verifyThat(andCount + redNotStarCount + starNotRedCount, is(orCount));
		verifyThat(orCount, is(unquotedCount));
	}

	private int getResultCount(String searchTerm) {
		search(searchTerm);
		return searchPage.countSearchResults();
	}

	//TODO
	@Test
	public void testFieldTextRestrictionOnPromotions(){
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.deleteAllPromotions();

		search("darth");

        searchPage.selectLanguage(Language.ENGLISH);

        searchPage.createAMultiDocumentPromotion(2);
		createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat");

//		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        searchPage.getDocLogo(1, new WebDriverWait(getDriver(), 10));
        searchPage.loadOrFadeWait();
		assertThat(searchPage.getPromotionSummarySize(), is(2));
		assertThat(searchPage.getPromotionSummaryLabels(), hasSize(2));

		final List<String> initialPromotionsSummary = searchPage.promotionsSummaryList(false);
		searchPage.showFieldTextOptions();
		searchPage.fieldTextAddButton().click();
		searchPage.fieldTextInput().sendKeys("MATCH{" + initialPromotionsSummary.get(0) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertThat(searchPage.getPromotionSummarySize(), is(1));
		assertThat(searchPage.getPromotionSummaryLabels(), hasSize(1));
		assertThat(searchPage.promotionsSummaryList(false).get(0), is(initialPromotionsSummary.get(0)));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + initialPromotionsSummary.get(1) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertThat(searchPage.getPromotionSummarySize(), is(1));
		assertThat(searchPage.getPromotionSummaryLabels(), hasSize(1));
		assertThat(searchPage.promotionsSummaryList(false).get(0), is(initialPromotionsSummary.get(1)));
	}

	@Test
	public void testFieldTextRestrictionOnPinToPositionPromotions(){
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.deleteAllPromotions();

		search("horse");
		searchPage.selectLanguage(Language.ENGLISH);

		final List<String> promotedDocs = searchPage.createAMultiDocumentPromotion(2);
		createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.addSearchTrigger("duck");
		createPromotionsPage.finishButton().click();
		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        searchPage.waitForSearchLoadIndicatorToDisappear();
		assertThat(promotedDocs.get(0) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(0)));
		assertThat(promotedDocs.get(1) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(1)));

		searchPage.showFieldTextOptions();
		searchPage.fieldTextAddButton().click();
		searchPage.fieldTextInput().sendKeys("WILD{*horse*}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.loadOrFadeWait();

		assertThat(promotedDocs.get(0) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(0)));
		assertThat(promotedDocs.get(1) + " should be visible", searchPage.getText(), containsString(promotedDocs.get(1)));	//TODO Seems like this shouldn't be visible
		assertThat("Wrong number of results displayed", searchPage.countSearchResults(), is(2));
		assertThat("Wrong number of pin to position labels displayed", searchPage.countPinToPositionLabels(), is(2));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + promotedDocs.get(0) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertThat(searchPage.getSearchResultTitle(1), is(promotedDocs.get(0)));
		assertThat(searchPage.countSearchResults(), is(1));
		assertThat(searchPage.countPinToPositionLabels(), is(1));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + promotedDocs.get(1) + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();

		assertThat(promotedDocs.get(1) + " not visible in the search title", searchPage.getSearchResultTitle(1), is(promotedDocs.get(1)));
		assertThat("Wrong number of search results", searchPage.countSearchResults(), is(1));
		assertThat("Wrong number of pin to position labels", searchPage.countPinToPositionLabels(), is(1));
	}

	// CSA-1818
	@Test
	public void testSearchResultsCount() {
		searchPage.selectLanguage(Language.ENGLISH);
		for (final String query : Arrays.asList("dog", "chips", "dinosaur", "melon", "art")) {
			search(query);
			final int firstPageResultsCount = searchPage.countSearchResults();

			searchPage.forwardToLastPageButton().click();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			verifyThat("number of results in title is consistent", searchPage.countSearchResults(), is(firstPageResultsCount));

			final int completePages = searchPage.getCurrentPageNumber() - 1;
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			final int expectedCount = completePages * SearchPage.RESULTS_PER_PAGE + lastPageDocumentsCount;
			verifyThat("number of results is as expected", searchPage.countSearchResults(), is(expectedCount));
		}
	}

	@Test
	public void testInvalidQueryTextNoKeywordsLinksDisplayed() {
		//TODO: map error messages to application type

        List<String> boolOperators = Arrays.asList("OR", "WHEN", "SENTENCE", "DNEAR");
        List<String> stopWords = Arrays.asList("a", "the", "of", "SOUNDEX"); //According to IDOL team SOUNDEX isn't considered a boolean operator without brackets
		searchPage.selectLanguage(Language.ENGLISH);

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            List<String> allTerms = ListUtils.union(boolOperators, stopWords);

            for (final String searchTerm : allTerms) {
				search(searchTerm);
                assertThat("Correct error message not present for searchterm: " + searchTerm, searchPage.getText(), containsString(Errors.Search.HOD));
            }

        } else if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
            for (final String searchTerm : boolOperators) {
                search(searchTerm);
                assertThat("Correct error message not present for searchterm: " + searchTerm + searchPage.getText(), searchPage.getText(), containsString("An error occurred executing the search action"));
                assertThat("Correct error message not present for searchterm: " + searchTerm, searchPage.getText(), containsString("An error occurred fetching the query analysis."));
                assertThat("Correct error message not present for searchterm: " + searchTerm, searchPage.getText(), containsString("Opening boolean operator"));
            }
            for (final String searchTerm : stopWords) {
                search(searchTerm);
                assertThat("Correct error message not present", searchPage.getText(), containsString("An error occurred executing the search action"));
                assertThat("Correct error message not present", searchPage.getText(), containsString("An error occurred fetching the query analysis."));
                assertThat("Correct error message not present", searchPage.getText(), containsString("No valid query text supplied"));
            }
        } else {
            fail("Application Type not recognised");
        }
	}

	@Test
	public void testAllowSearchOfStringsThatContainBooleansWithinThem() {
		final List<String> hiddenBooleansProximities = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "PARAGRAPHING", "NEARLY", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");
		for (final String hiddenBooleansProximity : hiddenBooleansProximities) {
			search(hiddenBooleansProximity);
			searchPage.loadOrFadeWait();
			assertThat(searchPage.getText(), not(containsString("Terminating boolean operator")));
		}
	}

	@Test
	public void testFromDateFilter() throws ParseException {
		search("Dog");
		// some indexes do not have dates
		new IndexFilter("wiki_eng").apply(searchPage);
		searchPage.waitForSearchLoadIndicatorToDisappear();
		final String firstResult = searchPage.getSearchResultTitle(1);
		final Date date = searchPage.getDateFromResult(1);
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.openFromDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(date);
		searchPage.closeFromDatePicker();
		assertThat("Document should still be displayed", searchPage.getSearchResultTitle(1), is(firstResult));

		searchPage.openFromDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, 1));
		searchPage.closeFromDatePicker();

		assertThat("Document should not be visible. Date filter not working", searchPage.getSearchResultTitle(1), not(firstResult));

		searchPage.openFromDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, -1));
		searchPage.closeFromDatePicker();
		assertThat("Document should be visible. Date filter not working", searchPage.getSearchResultTitle(1), is(firstResult));
	}

	@Test
	public void testUntilDateFilter() throws ParseException {
//		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		search("Dog");
		final String firstResult = searchPage.getSearchResultTitle(1);
		final Date date = searchPage.getDateFromResult(1);
		// plus 1 minute to be inclusive
		date.setTime(date.getTime() + 60000);
        logger.info("First Result: " + firstResult + " " + date);
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		try {
			datePicker.calendarDateSelect(date);
		} catch (final ElementNotVisibleException e) {
			for (final String label : searchPage.filterLabelList()) {
				assertThat("A 'From' date filter has been applied while only an 'Until' filter was selected by the user", label,not(containsString("From: ")));
			}
			assertThat("A 'From' date filter has been applied while only an 'Until' filter was selected by the user", searchPage.fromDateTextBox().getAttribute("value"), not(isEmptyOrNullString()));
			throw e;
		}
		searchPage.closeUntilDatePicker();
        logger.info(searchPage.untilDateTextBox().getAttribute("value"));
		assertThat("Document should still be displayed", searchPage.getSearchResultTitle(1), is(firstResult));

		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, -1));
		searchPage.closeUntilDatePicker();
        logger.info(searchPage.untilDateTextBox().getAttribute("value"));
        assertThat("Document should not be visible. Date filter not working", searchPage.getSearchResultTitle(1), not(firstResult));

		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, 1));
		searchPage.closeUntilDatePicker();
		assertThat("Document should be visible. Date filter not working", searchPage.getSearchResultTitle(1), is(firstResult));
	}

	@Test
	public void testFromDateAlwaysBeforeUntilDate() {
		search("food");
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:00 PM");
		searchPage.untilDateTextBox().sendKeys("04/05/2000 12:00 PM");
        searchPage.sortByRelevance();
		assertThat("Dates should be equal", searchPage.fromDateTextBox().getAttribute("value"), is(searchPage.untilDateTextBox().getAttribute("value")));

		searchPage.loadOrFadeWait();

		searchPage.fromDateTextBox().clear();
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:01 PM");
		//clicking sort by relevance because an outside click is needed for the changes to take place
		searchPage.sortByRelevance();
//		assertNotEquals("From date cannot be after the until date", searchPage.fromDateTextBox().getAttribute("value"), "04/05/2000 12:01 PM");
        assertThat("From date should be blank", searchPage.fromDateTextBox().getAttribute("value"), isEmptyOrNullString());

		searchPage.fromDateTextBox().clear();
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:00 PM");
		searchPage.untilDateTextBox().clear();
		searchPage.untilDateTextBox().sendKeys("04/05/2000 11:59 AM");
		searchPage.sortByRelevance();
//		assertEquals("Until date cannot be before the from date", searchPage.untilDateTextBox().getAttribute("value"),is(not("04/05/2000 11:59 AM")));
        assertThat("Until date should be blank", searchPage.untilDateTextBox().getAttribute("value"), isEmptyOrNullString());
	}

	@Test
	public void testFromDateEqualsUntilDate() throws ParseException {
		search("Search");
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
//		searchPage.openFromDatePicker();
//		searchPage.closeFromDatePicker();
//		searchPage.openUntilDatePicker();
//		searchPage.closeUntilDatePicker();

        searchPage.fromDateTextBox().sendKeys("12/12/2012 12:12");
        searchPage.untilDateTextBox().sendKeys("12/12/2012 12:12");

		assertThat("Datepicker dates are not equal", searchPage.fromDateTextBox().getAttribute("value"), is(searchPage.untilDateTextBox().getAttribute("value")));
		final Date date = searchPage.getDateFromFilter(searchPage.untilDateTextBox());

		searchPage.sendDateToFilter(DateUtils.addMinutes(date, 1), searchPage.untilDateTextBox());
        searchPage.sortByRelevance();
        assertThat(searchPage.untilDateTextBox().getAttribute("value"), is("12/12/2012 12:13"));

        searchPage.sendDateToFilter(DateUtils.addMinutes(date, -1), searchPage.untilDateTextBox());
        //clicking sort by relevance because an outside click is needed for the changes to take place
		searchPage.sortByRelevance();
        assertThat(searchPage.untilDateTextBox().getAttribute("value"), isEmptyOrNullString());
	}

	@Test
	public void testSortByRelevance() {
		search("string");
		searchPage.sortByRelevance();
		List<Float> weights = searchPage.getWeightsOnPage(5);

        logger.info("Weight of 0: " + weights.get(0));

        for (int i = 0; i < weights.size() - 1; i++) {
            logger.info("Weight of " + (i + 1) + ": " + weights.get(i + 1));

			assertThat("Weight of search result " + i + " is not greater that weight of search result " + (i + 1), weights.get(i), greaterThanOrEqualTo(weights.get(i + 1)));
		}

		searchPage.sortByDate();
		searchPage.sortByRelevance();
		weights = searchPage.getWeightsOnPage(5);
		for (int i = 0; i < weights.size() - 1; i++) {
			assertThat("Weight of search result " + i + " is not greater that weight of search result " + (i + 1), weights.get(i), greaterThanOrEqualTo(weights.get(i + 1)));
		}

		searchPage.sortByDate();
		search("paper packages");
		searchPage.sortByRelevance();
		weights = searchPage.getWeightsOnPage(5);
		for (int i = 0; i < weights.size() - 1; i++) {
			assertThat("Weight of search result " + i + " is not greater that weight of search result " + (i + 1), weights.get(i), greaterThanOrEqualTo(weights.get(i + 1)));
		}
	}

	@Test
	public void testSearchBarTextPersistsOnRefresh() {
		final String searchText = "Stay";
		search(searchText);

		// Change to promotions page since the search page will persist the query in the URL
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		getDriver().navigate().refresh();
		body = getBody();
		final String newSearchText = body.getTopNavBar().getSearchBarText();
		assertThat("search bar should be blank on refresh of a page that isn't the search page", newSearchText, is(searchText));
	}

	@Test
	public void testRelatedConceptsLinks() {
		String queryText = "frog";
		search(queryText);
		assertThat("The search bar has not retained the query text", topNavBar.getSearchBarText(), is(queryText));
		assertThat("'You searched for' section does not include query text", searchPage.youSearchedFor(), hasItem(queryText));
		assertThat("'Results for' heading text does not contain the query text", searchPage.getResultsForText(), containsString(queryText));

		for (int i = 0; i < 5; i++) {
			searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
			searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
			final int conceptsCount = searchPage.countRelatedConcepts();
			assertThat("Number of related concepts exceeds 50", conceptsCount, lessThanOrEqualTo(50));
			final int index = new Random().nextInt(conceptsCount);
			queryText = searchPage.getRelatedConcepts().get(index).getText();
			searchPage.relatedConcept(queryText).click();
			searchPage.waitForSearchLoadIndicatorToDisappear();

			assertThat("The search bar has not retained the query text", topNavBar.getSearchBarText(), is(queryText));
			final String[] words = queryText.split("\\s+");
			for (final String word : words) {
				assertThat("'You searched for' section does not include word: " + word + " for query text: " + queryText, searchPage.youSearchedFor(), hasItem(word));
			}
			assertThat("'Results for' heading text does not contain the query text: " + queryText, searchPage.getResultsForText(), containsString(queryText));
		}
	}

	@Test
	public void testRelatedConceptsDifferentInDifferentLanguages() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), Matchers.not(ApplicationType.HOSTED));

		search("France");
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> englishConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		searchPage.selectLanguage(Language.FRENCH);
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> frenchConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());

		assertThat("Concepts should be different in different languages", englishConcepts, not(containsInAnyOrder(frenchConcepts.toArray())));

		searchPage.selectLanguage(Language.ENGLISH);
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> secondEnglishConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		assertThat("Related concepts have changed on second search of same query text", englishConcepts, contains(secondEnglishConcepts.toArray()));
	}

	// CSA-1819
	@Test
	public void testNavigateToLastPageOfSearchResultsAndEditUrlToTryAndNavigateFurther() {
        search("nice");
		searchPage.forwardToLastPageButton().click();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		final int currentPage = searchPage.getCurrentPageNumber();
		final String docTitle = searchPage.getSearchResultTitle(1);
		final String url = getDriver().getCurrentUrl();
		assertThat("Url and current page number are out of sync", url, containsString("nice/" + currentPage));

		final String illegitimateUrl = url.replace("nice/" + currentPage, "nice/" + (currentPage + 5));
		getDriver().navigate().to(illegitimateUrl);
		searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        assertThat("Page should still have results", searchPage, not(containsText(Errors.Search.NO_RESULTS)));
		assertThat("Page should not have thrown an error", searchPage, not(containsText(Errors.Search.HOD)));
		assertThat("Page number should not have changed", currentPage, is(searchPage.getCurrentPageNumber()));
		assertThat("Url should have reverted to original url", url, is(getDriver().getCurrentUrl()));
		assertThat("Error message should not be showing", searchPage.isErrorMessageShowing(), is(false));
		assertThat("Search results have changed on last page", docTitle, is(searchPage.getSearchResultTitle(1)));
	}

	@Test
	public void testNoRelatedConceptsIfNoResultsFound() {
		final String garbageQueryText = "garbagedjlsfjijlsf";
		search(garbageQueryText);

        String errorMessage = "Garbage text returned results. garbageQueryText string needs changed to be more garbage like";
		assertThat(errorMessage, searchPage.getText(), containsString("No results found"));
		assertThat(errorMessage, searchPage.countSearchResults(), is(0));

		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
        assertThat("If there are no search results there should be no related concepts", searchPage.getText(), containsString("No related concepts found"));
	}

	@Test
	//TODO parametric values aren't working - file ticket
	public void testParametricValuesLoads() throws InterruptedException {
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.PARAMETRIC_VALUES);
		Thread.sleep(20000);
		assertThat("Load indicator still visible after 20 seconds", searchPage.parametricValueLoadIndicator().isDisplayed(), is(false));
	}

	@Test
	public void testContentType(){
		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			selectNewsEngIndex();
			searchPage.findElement(By.xpath("//label[text()[contains(.,'Public')]]/../i")).click();
		}

		search("Alexis");

		searchPage.openParametricValuesList();
		searchPage.loadOrFadeWait();

		try {
			new WebDriverWait(getDriver(), 30)
					.withMessage("Waiting for parametric values list to load")
					.until(new ExpectedCondition<Boolean>() {
						@Override
						public Boolean apply(WebDriver driver) {
							return !searchPage.parametricValueLoadIndicator().isDisplayed();
						}
					});
		} catch (TimeoutException e) {
			fail("Parametric values did not load");
		}

		int results = searchPage.filterByContentType("TEXT/PLAIN");

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		searchPage.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.loadOrFadeWait();

		assertThat(searchPage.searchTitle().findElement(By.xpath(".//..//span")).getText(), is("(" + results + ")"));

		searchPage.forwardToLastPageButton().click();

		int resultsTotal = (searchPage.getCurrentPageNumber() - 1) * 6;
		resultsTotal += searchPage.visibleDocumentsCount();

		assertThat(resultsTotal, is(results));
	}

	@Test
	public void testSearchTermHighlightedInResults() {
		String searchTerm = "Tiger";

		search(searchTerm);

		for(int i = 0; i < 3; i++) {
			for (WebElement searchElement : getDriver().findElements(By.xpath("//div[contains(@class,'search-results-view')]//p//*[contains(text(),'" + searchTerm + "')]"))) {
				if (searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
					verifyThat(searchElement.getText(), CoreMatchers.containsString(searchTerm));
				}
				verifyThat(searchElement.getTagName(), is("a"));
				verifyThat(searchElement.getAttribute("class"), is("query-text"));

				WebElement parent = searchElement.findElement(By.xpath(".//.."));
				verifyThat(parent.getTagName(), is("span"));
				verifyThat(parent.getAttribute("class"), CoreMatchers.containsString("label"));
			}
			searchPage.forwardPageButton().click();
		}
	}

	@Test
	//CSA1708
	public void testParametricLabelsNotUndefined(){
		new Search(getApplication(),getElementFactory(),"simpsons").applyFilter(new IndexFilter("default_index")).apply();

		searchPage.filterByContentType("TEXT/HTML");

		for(WebElement filter : searchPage.findElements(By.cssSelector(".filter-display-view span"))){
			assertThat(filter.getText().toLowerCase(),not(containsString("undefined")));
		}
	}
}
