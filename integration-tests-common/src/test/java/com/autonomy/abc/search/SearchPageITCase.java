package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
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

    String havenErrorMessage = "Haven OnDemand returned an error while executing the search action";

	@Before
	public void setUp() throws MalformedURLException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Initial thread.sleep failed");
		}
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            //Select news_ing index because I'm tired of adding lots of files to indexes
            searchPage.findElement(By.xpath("//label[text()[contains(.,'Public')]]/../i")).click();
            selectNewsEngIndex();
            searchPage.waitForSearchLoadIndicatorToDisappear();
        }
	}

	//TODO move this to SearchBase (and refactor code)
    private void selectNewsEngIndex() {
		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()[contains(.,'news_eng')]]"))).click();
		}
    }

	private void search(String searchTerm){
		logger.info("Searching for: '"+searchTerm+"'");
		topNavBar.search(searchTerm);
		searchPage.waitForSearchLoadIndicatorToDisappear();
		assertNotEquals(searchPage.getText(), contains(havenErrorMessage));
	}

    @Test
	public void testUnmodifiedResultsToggleButton(){
        new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

        assertTrue("Page should be showing modified results", searchPage.modifiedResultsShown());
		assertThat("Url incorrect", getDriver().getCurrentUrl(), containsString("/modified"));

		searchPage.modifiedResultsCheckBox().click();
        assertTrue("Page should not be showing modified results", !searchPage.modifiedResultsShown());
		assertThat("Url incorrect", getDriver().getCurrentUrl(), containsString("/unmodified"));

		searchPage.modifiedResultsCheckBox().click();
        assertTrue("Page should be showing modified results", searchPage.modifiedResultsShown());
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

		assertTrue("The show more promotions button is not visible", searchPage.showMorePromotionsButton().isDisplayed());
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

                assertThat(searchPage.getText(),containsString(havenErrorMessage));
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
                assertThat(searchPage.getText(),containsString(havenErrorMessage));
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
		searchPage.selectLanguage("Arabic");
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
				searchPage.getPromotionBucketElementByTrimmedTitle(docTitle).click();

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
		String docTitle = searchPage.getSearchResultTitle(1);
		search("1");

		for (final String language : Arrays.asList("English", "Afrikaans", "French", "Arabic", "Urdu", "Hindi", "Chinese", "Swahili")) {
			searchPage.selectLanguage(language);
			assertEquals(language, searchPage.getSelectedLanguage());

			searchPage.waitForSearchLoadIndicatorToDisappear();
			assertNotEquals(docTitle, searchPage.getSearchResultTitle(1));

			docTitle = searchPage.getSearchResultTitle(1);
		}
	}

	@Test
	public void testBucketEmptiesWhenLanguageChangedInURL() {
		search("arc");
		searchPage.selectLanguage("French");
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.promoteTheseDocumentsButton().click();

		for (int i = 1; i <=4; i++) {
			searchPage.searchResultCheckbox(i).click();
		}

		assertEquals(4, searchPage.promotionsBucketWebElements().size());

		final String url = getDriver().getCurrentUrl().replace("french", "arabic");
		getDriver().get(url);
		searchPage = getElementFactory().getSearchPage();
		searchPage.loadOrFadeWait();
		assertThat("Have not navigated back to search page with modified url " + url, searchPage.promoteThisQueryButton().isDisplayed());
		assertEquals(0, searchPage.promotionsBucketWebElements().size());
	}

	@Test
	public void testLanguageDisabledWhenBucketOpened() {
		//This test currently fails because language dropdown is not disabled when the promotions bucket is open
		searchPage.selectLanguage("English");
		search("al");
		searchPage.loadOrFadeWait();
		assertThat("Languages should be enabled", !searchPage.isAttributePresent(searchPage.languageButton(), "disabled"));

		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertEquals("There should be one document in the bucket", 1, searchPage.promotionsBucketList().size());
		searchPage.selectLanguage("French");
		assertFalse("The promotions bucket should close when the language is changed", searchPage.promotionsBucket().isDisplayed());

		searchPage.promoteTheseDocumentsButton().click();
		assertEquals("There should be no documents in the bucket after changing language", 0, searchPage.promotionsBucketList().size());

		searchPage.selectLanguage("English");
		assertFalse("The promotions bucket should close when the language is changed", searchPage.promotionsBucket().isDisplayed());
	}

	@Test
	public void testSearchAlternateScriptToSelectedLanguage() {
		for (final String language : Arrays.asList("French", "English", "Arabic", "Urdu", "Hindi", "Chinese")) {
			searchPage.selectLanguage(language);

			for (final String script : Arrays.asList("निर्वाण", "العربية", "עברית", "сценарий", "latin", "ελληνικά", "ქართული", "བོད་ཡིག")) {
				search(script);
				searchPage.loadOrFadeWait();
				assertThat("Undesired error message for language: " + language + " with script: " + script, searchPage.findElement(By.cssSelector(".search-results-view")).getText(),not(containsString("error")));
			}
		}
	}

    org.slf4j.Logger logger = LoggerFactory.getLogger(SearchPageITCase.class);

	@Test
	public void testFieldTextFilter() {
		search("war");
		searchPage.selectLanguage("English");
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
//        indexesWarn();
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
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(havenErrorMessage)));
		searchPage.waitForSearchLoadIndicatorToDisappear();

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
		search("boer");

        if(getConfig().getType().equals(ApplicationType.ON_PREM)) {
            searchPage.selectLanguage("Afrikaans");
        } else {
            languageWarn();
        }

        searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
//        indexesWarn();
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
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(havenErrorMessage)));
		assertEquals(firstSearchResult, searchPage.getSearchResultTitle(1));

		searchPage.fieldTextEditButton().click();
		searchPage.fieldTextInput().clear();
		searchPage.fieldTextInput().sendKeys("MATCH{" + secondSearchResult + "}:DRETITLE");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(havenErrorMessage)));
		assertEquals(secondSearchResult, searchPage.getSearchResultTitle(1));
	}

    private void indexesWarn() {
        logger.warn("Some indexes don't work, only using news_eng and default");
    }

    private void languageWarn() {
        logger.warn("Languages not implemented; using English instead");
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
//        searchPage.waitForSearchLoadIndicatorToDisappear();
//        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
//            selectNewsEngIndex();
//        }

        search("leg");

        searchPage.selectLanguage("English");

        int initialSearchCount = searchPage.countSearchResults();
		search("leg[2:2]");
		searchPage.loadOrFadeWait();
		assertThat("Failed with the following search term: leg[2:2]  Search count should have reduced on initial search 'leg'",
				initialSearchCount, greaterThan(searchPage.countSearchResults()));

		search("red");
		searchPage.loadOrFadeWait();
		initialSearchCount = searchPage.countSearchResults();
		search("red star");
		searchPage.loadOrFadeWait();
		final int secondSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red star  Search count should have increased on initial search: red",
                initialSearchCount, lessThan(secondSearchCount));

		search("\"red star\"");
		searchPage.loadOrFadeWait();
		final int thirdSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: '\"red star\"'  Search count should have reduced on initial search: red star",
				secondSearchCount, greaterThan(thirdSearchCount));

		search("red NOT star");
		searchPage.loadOrFadeWait();
		final int redNotStar = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red NOT star  Search count should have reduced on initial search: red",
				initialSearchCount, greaterThan(redNotStar));

        search("star");
        searchPage.loadOrFadeWait();
        final int star = searchPage.countSearchResults();

		search("star NOT red");
		searchPage.loadOrFadeWait();
		final int starNotRed = searchPage.countSearchResults();
        assertThat("Failed with the following search term: star NOT red  Search count should have reduced on initial search: star",
				star, greaterThan(starNotRed));

		search("red OR star");
		searchPage.loadOrFadeWait();
		assertThat("Failed with the following search term: red OR star  Search count should be the same as initial search: red star",
				secondSearchCount, is(searchPage.countSearchResults()));

		search("red AND star");
		searchPage.loadOrFadeWait();
		final int fourthSearchCount = searchPage.countSearchResults();
        assertThat("Failed with the following search term: red AND star  Search count should have reduced on initial search: red star",
				secondSearchCount, greaterThan(fourthSearchCount));
        assertThat("Failed with the following search term: red AND star  Search count should have increased on initial search: \"red star\"",
				thirdSearchCount, lessThan(fourthSearchCount));
		assertThat("Sum of 'A NOT B', 'B NOT A' and 'A AND B' should equal 'A OR B' where A is: red  and B is: star",
				fourthSearchCount + redNotStar + starNotRed, is(secondSearchCount));
	}

    //TODO
	@Test
	public void testFieldTextRestrictionOnPromotions(){
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.deleteAllPromotions();

		search("darth");

        searchPage.selectLanguage("English");

        searchPage.createAMultiDocumentPromotion(2);
		createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion("Sponsored", "boat");

//		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        searchPage.getDocLogo(1,new WebDriverWait(getDriver(),10));
        searchPage.loadOrFadeWait();
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
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.deleteAllPromotions();

		search("horse");
		searchPage.selectLanguage("English");

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
//        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
//            selectNewsEngIndex();
//        }

		searchPage.selectLanguage("English");
		for (final String query : Arrays.asList("dog", "chips", "dinosaur", "melon", "art")) {
			logger.info("String = '" + query + "'");
			search(query);
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

        List<String> boolOperators = Arrays.asList("OR", "WHEN", "SENTENCE", "DNEAR");
        List<String> stopWords = Arrays.asList("a", "the", "of", "SOUNDEX"); //According to IDOL team SOUNDEX isn't considered a boolean operator without brackets

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            languageWarn();

            List<String> allTerms = ListUtils.union(boolOperators,stopWords);

            for (final String searchTerm : allTerms) {
				search(searchTerm);
                assertThat("Correct error message not present for searchterm: " + searchTerm, searchPage.getText(), containsString("Haven OnDemand returned an error while executing the search action"));
            }

        } else if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
            searchPage.selectLanguage("English");
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
//		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		search("Dog");
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

	//TODO - doesn't seem to be functioning properly
	@Test
	public void testUntilDateFilter() throws ParseException {
//		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		search("Dog");
		final String firstResult = searchPage.getSearchResultTitle(1);
		final Date date = searchPage.getDateFromResult(1);
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
			assertFalse("A 'From' date filter has been applied while only an 'Until' filter was selected by the user", searchPage.fromDateTextBox().getAttribute("value").isEmpty());
			throw e;
		}
		searchPage.closeUntilDatePicker();
        logger.info(searchPage.untilDateTextBox().getAttribute("value"));
		assertEquals("Document should still be displayed", firstResult, searchPage.getSearchResultTitle(1));

		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, -1));
		searchPage.closeUntilDatePicker();
        logger.info(searchPage.untilDateTextBox().getAttribute("value"));
        assertEquals("Document should not be visible. Date filter not working", firstResult, not(searchPage.getSearchResultTitle(1)));

		searchPage.openUntilDatePicker();
		datePicker = new DatePicker(searchPage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addMinutes(date, 1));
		searchPage.closeUntilDatePicker();
		assertEquals("Document should be visible. Date filter not working", firstResult, searchPage.getSearchResultTitle(1));
	}

	@Test
	public void testFromDateAlwaysBeforeUntilDate() {
		search("food");
		searchPage.expandFilter(SearchBase.Filter.FILTER_BY);
		searchPage.expandSubFilter(SearchBase.Filter.DATES);
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:00 PM");
		searchPage.untilDateTextBox().sendKeys("04/05/2000 12:00 PM");
        searchPage.sortByRelevance();
		assertEquals("Dates should be equal", searchPage.fromDateTextBox().getAttribute("value"), searchPage.untilDateTextBox().getAttribute("value"));

		searchPage.loadOrFadeWait();

		searchPage.fromDateTextBox().clear();
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:01 PM");
		//clicking sort by relevance because an outside click is needed for the changes to take place
		searchPage.sortByRelevance();
//		assertNotEquals("From date cannot be after the until date", searchPage.fromDateTextBox().getAttribute("value"), "04/05/2000 12:01 PM");
        assertEquals("From date should be blank", searchPage.fromDateTextBox().getAttribute("value").toString(), "");

		searchPage.fromDateTextBox().clear();
		searchPage.fromDateTextBox().sendKeys("04/05/2000 12:00 PM");
		searchPage.untilDateTextBox().clear();
		searchPage.untilDateTextBox().sendKeys("04/05/2000 11:59 AM");
		searchPage.sortByRelevance();
//		assertEquals("Until date cannot be before the from date", searchPage.untilDateTextBox().getAttribute("value"),is(not("04/05/2000 11:59 AM")));
        assertEquals("Until date should be blank",searchPage.untilDateTextBox().getAttribute("value").toString(),"");
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

		assertEquals("Datepicker dates are not equal", searchPage.fromDateTextBox().getAttribute("value"), searchPage.untilDateTextBox().getAttribute("value"));
		final Date date = searchPage.getDateFromFilter(searchPage.untilDateTextBox());

		searchPage.sendDateToFilter(DateUtils.addMinutes(date, 1), searchPage.untilDateTextBox());
        searchPage.sortByRelevance();
        assertThat(searchPage.untilDateTextBox().getAttribute("value"), is("12/12/2012 12:13"));

        searchPage.sendDateToFilter(DateUtils.addMinutes(date, -1), searchPage.untilDateTextBox());
        //clicking sort by relevance because an outside click is needed for the changes to take place
		searchPage.sortByRelevance();
        assertEquals("", searchPage.untilDateTextBox().getAttribute("value"));
	}

	@Test
	public void testSortByRelevance() {
		search("string");
		searchPage.sortByRelevance();
		List<Float> weights = searchPage.getWeightsOnPage(5);

        logger.info("Weight of 0: " + weights.get(0));

        for (int i = 0; i < weights.size() - 1; i++) {
            logger.info("Weight of "+(i+1)+": "+weights.get(i+1));

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
		assertEquals("search bar should be blank on refresh of a page that isn't the search page", newSearchText, searchText);
	}

	@Test
	public void testRelatedConceptsLinks() {
		String queryText = "frog";
		search(queryText);
		assertEquals("The search bar has not retained the query text", topNavBar.getSearchBarText(), queryText);
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

			assertEquals("The search bar has not retained the query text", topNavBar.getSearchBarText(), queryText);
			final String[] words = queryText.split("\\s+");
			for (final String word : words) {
				assertThat("'You searched for' section does not include word: " + word + " for query text: " + queryText, searchPage.youSearchedFor(), hasItem(word));
			}
			assertThat("'Results for' heading text does not contain the query text: " + queryText, searchPage.getResultsForText(), containsString(queryText));
		}
	}

	@Test
	public void testRelatedConceptsDifferentInDifferentLanguages() {
		search("France");
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> englishConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		searchPage.selectLanguage("French");
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> frenchConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());

		assertThat("Concepts should be different in different languages", englishConcepts, not(containsInAnyOrder(frenchConcepts.toArray())));

		searchPage.selectLanguage("English");
		searchPage.expandFilter(SearchBase.Filter.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> secondEnglishConcepts = searchPage.webElementListToStringList(searchPage.getRelatedConcepts());
		assertThat("Related concepts have changed on second search of same query text", englishConcepts, contains(secondEnglishConcepts.toArray()));
	}

	@Test
	public void testNavigateToLastPageOfSearchResultsAndEditUrlToTryAndNavigateFurther() {
//        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
//			selectNewsEngIndex();
//        }

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
		//TODO failing here wrongly
        assertThat("Page should still have results", searchPage.getText(), not(containsString("No results found...")));
		assertThat("Page should not have thrown an error", searchPage.getText(), not(containsString(havenErrorMessage)));
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
		assertEquals(errorMessage, 0, searchPage.countSearchResults());

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
}
