package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.Pagination;
import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.search.DocumentViewer;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.promotions.PinToPositionPromotion;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.search.*;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Waits;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPageITCase extends ABCTestBase {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private SearchPage searchPage;
	private TopNavBar topNavBar;
	private SearchService searchService;

	public SearchPageITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = getElementFactory().getTopNavBar();
		searchService = getApplication().searchService();
		searchPage = searchService.search("example");
	}

	private void search(String searchTerm){
		logger.info("Searching for: '" + searchTerm + "'");
		searchPage = searchService.search(searchTerm);
	}

	private void search(SearchQuery query) {
		logger.info("Searching for: " + query + "");
		searchPage = searchService.search(query);
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
		assertThat("Search title text is wrong", searchPage.getHeadingSearchTerm(), is("dog"));

		search("cat");
		assertThat("Search title text is wrong", searchPage.getHeadingSearchTerm(), is("cat"));

		search("ElEPhanT");
		assertThat("Search title text is wrong", searchPage.getHeadingSearchTerm(), is("ElEPhanT"));
	}

	@Test
	public void testPromoteButton(){
		searchPage.promoteTheseDocumentsButton().click();
		Waits.loadOrFadeWait();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be enabled", ElementUtil.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount(), is(0));

		searchPage.searchResultCheckbox(1).click();
		assertThat("Promote these items button should be enabled", !ElementUtil.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(1));

		searchPage.promotionsBucketClose();
		assertThat("Promoted items bucket has not appeared", searchPage.getText(), not(containsString("Select Items to Promote")));

		searchPage.promoteTheseDocumentsButton().click();
		Waits.loadOrFadeWait();
		assertThat("Promoted items bucket has not appeared", searchPage.promotionsBucket().isDisplayed());
		assertThat("Promote these items button should not be enabled", ElementUtil.isAttributePresent(searchPage.promoteTheseItemsButton(), "disabled"));
		assertThat("Promoted items count should equal 0", searchPage.promotedItemsCount(), is(0));
	}

	@Test
	public void testAddFilesToPromoteBucket() {
		searchPage.promoteTheseDocumentsButton().click();
		Waits.loadOrFadeWait();

		for (int i = 1; i < 7; i++) {
			ElementUtil.scrollIntoView(searchPage.searchResultCheckbox(i), getDriver());
			searchPage.searchResultCheckbox(i).click();
			assertThat("Promoted items count not correct", searchPage.promotedItemsCount(),is(i));
		}

		for (int j = 6; j > 0; j--) {
			ElementUtil.scrollIntoView(searchPage.searchResultCheckbox(j), getDriver());
			searchPage.searchResultCheckbox(j).click();
			assertThat("Promoted items count not correct", searchPage.promotedItemsCount(), is(j - 1));
		}

		searchPage.promotionsBucketClose();
	}

	@Test
	public void testSearchResultsPagination() {
		search("grass");
		Waits.loadOrFadeWait();
		assertThat(searchPage.resultsPaginationButton(Pagination.FIRST), disabled());
		assertThat(searchPage.resultsPaginationButton(Pagination.PREVIOUS), disabled());

		searchPage.switchResultsPage(Pagination.NEXT);
		assertThat(searchPage.resultsPaginationButton(Pagination.FIRST), not(disabled()));
		assertThat(searchPage.resultsPaginationButton(Pagination.PREVIOUS), not(disabled()));
		assertThat(searchPage.getCurrentPageNumber(), is(2));

		searchPage.switchResultsPage(Pagination.NEXT);
		searchPage.switchResultsPage(Pagination.NEXT);
		searchPage.switchResultsPage(Pagination.PREVIOUS);
		assertThat(searchPage.getCurrentPageNumber(), is(3));

		searchPage.switchResultsPage(Pagination.FIRST);
		assertThat(searchPage.getCurrentPageNumber(), is(1));

		searchPage.switchResultsPage(Pagination.LAST);
		assertThat(searchPage.resultsPaginationButton(Pagination.LAST), disabled());
		assertThat(searchPage.resultsPaginationButton(Pagination.NEXT), disabled());

		final int numberOfPages = searchPage.getCurrentPageNumber();
		for (int i = numberOfPages - 1; i > 0; i--) {
			searchPage.switchResultsPage(Pagination.PREVIOUS);
			assertThat(searchPage.getCurrentPageNumber(), is(i));
			assertThat(getDriver().getCurrentUrl(), endsWith(String.valueOf(i)));
		}

		for (int j = 2; j < numberOfPages + 1; j++) {
			searchPage.switchResultsPage(Pagination.NEXT);
			assertThat(searchPage.getCurrentPageNumber(), is(j));
			assertThat(getDriver().getCurrentUrl(), endsWith(String.valueOf(j)));
		}
	}

	// This used to fail because the predict=false parameter was not added to our query actions
	@Test
	public void testPaginationAndBackButton() {
		search("safe");
		searchPage.switchResultsPage(Pagination.LAST);
		assertThat(searchPage.resultsPaginationButton(Pagination.LAST), disabled());
		assertThat(searchPage.resultsPaginationButton(Pagination.NEXT), disabled());
		assertThat(searchPage.resultsPaginationButton(Pagination.PREVIOUS), not(disabled()));
		assertThat(searchPage.resultsPaginationButton(Pagination.FIRST), not(disabled()));
		final int lastPage = searchPage.getCurrentPageNumber();

		getDriver().navigate().back();
		assertThat("Back button has not brought the user back to the first page", searchPage.getCurrentPageNumber(), is(1));

		getDriver().navigate().forward();
		assertThat("Forward button has not brought the user back to the last page", searchPage.getCurrentPageNumber(), is(lastPage));
	}

	@Test
	public void testAddDocumentToPromotionsBucket() {
		search("horse");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.searchResultCheckbox(1).click();
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(1));
		assertThat("File in bucket description does not match file added", searchPage.getSearchResult(1).getTitleString(), equalToIgnoringCase(searchPage.bucketDocumentTitle(1)));
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
		Promotion promotion = new SpotlightPromotion("boat");

		PromotionService promotionService = getApplication().promotionService();
		promotionService.deleteAll();
		promotionService.setUpPromotion(promotion, "freeze", 18);

		try {
			PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(promotion);

			promotionsDetailPage.getTriggerForm().clickTrigger("boat");
			searchPage = getElementFactory().getSearchPage();
			searchPage.waitForPromotionsLoadIndicatorToDisappear();

			assertThat("two promotions visible", searchPage.getPromotionSummarySize(), is(2));
			assertThat("can show more", searchPage.showMorePromotionsButton(), displayed());

			searchPage.showMorePromotions();
			assertThat("showing more", searchPage.getPromotionSummarySize(), is(5));

			searchPage.showLessPromotions();
			assertThat("showing less", searchPage.getPromotionSummarySize(), is(2));

			searchPage.showMorePromotions();
			assertThat("showing more again", searchPage.getPromotionSummarySize(), is(5));

			searchPage.switchPromotionPage(Pagination.NEXT);
			logger.info("on page 2");
			verifyPromotionPagination(true, true);

			searchPage.switchPromotionPage(Pagination.NEXT);
			searchPage.switchPromotionPage(Pagination.NEXT);
			logger.info("on last page");
			verifyPromotionPagination(true, false);

			for (int unused=0; unused < 3; unused++) {
				searchPage.switchPromotionPage(Pagination.PREVIOUS);
			}
			logger.info("on first page");
			verifyPromotionPagination(false, true);

			searchPage.switchPromotionPage(Pagination.LAST);
			logger.info("on last page");
			verifyPromotionPagination(true, false);

			searchPage.switchPromotionPage(Pagination.FIRST);
			logger.info("on first page");
			verifyPromotionPagination(false, true);
		} finally {
			promotionService.deleteAll();
		}
	}

	private void verifyPromotionPagination(boolean previousEnabled, boolean nextEnabled) {
		verifyButtonEnabled("back to start", searchPage.promotionPaginationButton(Pagination.FIRST), previousEnabled);
		verifyButtonEnabled("back", searchPage.promotionPaginationButton(Pagination.PREVIOUS), previousEnabled);
		verifyButtonEnabled("forward", searchPage.promotionPaginationButton(Pagination.NEXT), nextEnabled);
		verifyButtonEnabled("forward to end", searchPage.promotionPaginationButton(Pagination.LAST), nextEnabled);
	}

	private void verifyButtonEnabled(String name, WebElement element, boolean enabled) {
		if (enabled) {
			verifyThat(name + " button enabled", element, not(hasClass("disabled")));
		} else {
			verifyThat(name + " button disabled", element, hasClass("disabled"));
		}
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

            search(testSearchTerms.get(searchTerm));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(searchErrorMessage));
            assertThat(correctErrorMessageNotShown, searchPage.getText(), containsString(bracketMismatch));
        } else {
            fail("Config type not recognised");
        }
	}

    //TODO there are some which contain helpful error messages?
	@Test
	@RelatedTo("IOD-8454")
	public void testSearchQuotationMarks() {
        List<String> emptyPhrases = Arrays.asList("\"\"","\" \"");
		List<String> unclosedPhrases = Arrays.asList("\"","\"word","\" word","\" wo\"rd\"");

		String emptyError = Errors.Search.NO_TEXT;
		String unclosedError = Errors.Search.QUOTES;

		// HOD should return better errors
        if(getConfig().getType().equals(ApplicationType.HOSTED)){
			emptyError = Errors.Search.HOD;
			unclosedError = Errors.Search.HOD;
        }
		for (String empty : emptyPhrases) {
			search(empty);
			verifyThat(searchPage, containsText(emptyError));
		}
		for (String unclosed : unclosedPhrases) {
			search(unclosed);
			verifyThat(searchPage, containsText(unclosedError));
		}
	}

	@Test
	//TODO seems to be failing within VM - investigate futher
	public void testDeleteDocsFromWithinBucket() {
		search("sabre");
		searchPage.promoteTheseDocumentsButton().click();
		searchPage.addToBucket(4);

		final List<String> bucketList = searchPage.promotionsBucketList();
		assertThat("There should be four documents in the bucket", bucketList.size(), is(4));
		assertThat(searchPage.promoteTheseDocumentsButton(), disabled());
		assertThat(searchPage.promoteTheseItemsButton(), displayed());

		searchPage.emptyBucket();

		assertThat("promote button should be disabled when bucket has no documents", searchPage.promoteTheseItemsButton(), disabled());

		search("tooth");
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList(), empty());

		searchPage.searchResultCheckbox(5).click();
		final List<String> docTitles = new ArrayList<>();
		docTitles.add(searchPage.getSearchResult(5).getTitleString());
		searchPage.switchResultsPage(Pagination.NEXT);
		searchPage.searchResultCheckbox(3).click();
		docTitles.add(searchPage.getSearchResult(3).getTitleString());

		final List<String> bucketListNew = searchPage.promotionsBucketList();
		assertThat("Wrong number of documents in the bucket", bucketListNew, hasSize(2));
		assertThat(bucketListNew, hasSize(docTitles.size()));

		for(String docTitle : docTitles){
			assertThat(bucketListNew, hasItem(equalToIgnoringCase(docTitle)));
		}

		searchPage.deleteDocFromWithinBucket(docTitles.get(1));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList(), hasSize(1));
		assertThat("Document should still be in the bucket", searchPage.promotionsBucketList(),hasItem(equalToIgnoringCase(docTitles.get(0))));
		assertThat("Document should no longer be in the bucket", searchPage.promotionsBucketList(),not(hasItem(equalToIgnoringCase(docTitles.get(1)))));
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(3).isSelected());

		searchPage.switchResultsPage(Pagination.PREVIOUS);
		searchPage.deleteDocFromWithinBucket(docTitles.get(0));
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList(), empty());
		assertThat("Checkbox still selected when doc deleted from bucket", !searchPage.searchResultCheckbox(5).isSelected());
		assertThat("promote button should be disabled when bucket has no documents", searchPage.promoteTheseItemsButton(), disabled());
	}

	@Test
	public void testViewFrame() throws InterruptedException {
		search("army");
		searchPage.waitForSearchLoadIndicatorToDisappear();

		for (int page = 1; page <= 2; page++) {
			for (int result = 1; result <= 6; result++) {
				Waits.loadOrFadeWait();
				searchPage.docLogo(result).click();
				checkViewResult();
				searchPage.getSearchResult(1).title().click();
				checkViewResult();
			}

			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	private void checkViewResult() {
		final String handle = getDriver().getWindowHandle();
		DocumentViewer docViewer = DocumentViewer.make(getDriver());

		getDriver().switchTo().frame(docViewer.frame());
		verifyThat(getDriver().findElement(By.xpath(".//*")), not(hasTextThat(isEmptyOrNullString())));

		getDriver().switchTo().window(handle);
		docViewer.close();
	}

	@Test
	public void testViewFromBucketLabel() throws InterruptedException {
		// TODO: add Arabic test to OP
		//Testing in Arabic because in some instances not latin urls have been encoded incorrectly
		search("جيمس");
		searchPage.selectLanguage(Language.ARABIC);

        search("Engineer");

		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.promoteTheseDocumentsButton().click();

		for (int j = 1; j <=2; j++) {
			for (int i = 1; i <= 3; i++) {
				final String handle = getDriver().getWindowHandle();
				searchPage.searchResultCheckbox(i).click();
				final String docTitle = searchPage.getSearchResult(i).getTitleString();
				ElementUtil.scrollIntoViewAndClick(searchPage.promotionBucketElementByTitle(docTitle), getDriver());

				DocumentViewer viewer = DocumentViewer.make(getDriver());
				getDriver().switchTo().frame(viewer.frame());
				verifyThat("view frame displays", getDriver().findElement(By.xpath(".//*")), containsText(docTitle));

				getDriver().switchTo().window(handle);
				viewer.close();
			}

			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	@Test
	public void testChangeLanguage() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), Matchers.not(ApplicationType.HOSTED));
		String docTitle = searchPage.getSearchResult(1).getTitleString();
		search("1");

		List<Language> languages = Arrays.asList(Language.ENGLISH, Language.AFRIKAANS, Language.FRENCH, Language.ARABIC, Language.URDU, Language.HINDI, Language.CHINESE, Language.SWAHILI);
		for (final Language language : languages) {
			searchPage.selectLanguage(language);
			assertThat(searchPage.getSelectedLanguage(), is(language));

			searchPage.waitForSearchLoadIndicatorToDisappear();
			assertThat(searchPage.getSearchResult(1).getTitleString(), not(docTitle));

			docTitle = searchPage.getSearchResult(1).getTitleString();
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
		Waits.loadOrFadeWait();
		assertThat("Have not navigated back to search page with modified url " + url, searchPage.promoteThisQueryButton().isDisplayed());
		assertThat(searchPage.promotionsBucketWebElements(), hasSize(0));
	}

	@Test
	public void testLanguageDisabledWhenBucketOpened() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), Matchers.not(ApplicationType.HOSTED));
		//This test currently fails because language dropdown is not disabled when the promotions bucket is open
		searchPage.selectLanguage(Language.ENGLISH);
		search("al");
		Waits.loadOrFadeWait();
		assertThat("Languages should be enabled", !ElementUtil.isAttributePresent(searchPage.languageButton(), "disabled"));

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
				Waits.loadOrFadeWait();
				assertThat("Undesired error message for language: " + language + " with script: " + script, searchPage.findElement(By.cssSelector(".search-results-view")).getText(),not(containsString("error")));
			}
		}
	}

    @Test
	public void testFieldTextInputDisappearsOnOutsideClick() {
		searchPage.expand(SearchBase.Facet.FIELD_TEXT);
		assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());

		WebElement fieldTextInputElement = searchPage.fieldTextInput().getElement();

		searchPage.fieldTextAddButton().click();
		assertThat("Field text add button visible", !searchPage.fieldTextAddButton().isDisplayed());
		assertThat("Field text input not visible", fieldTextInputElement, displayed());

		searchPage.fieldTextInput().getElement().click();
		assertThat("Field text add button visible", !searchPage.fieldTextAddButton().isDisplayed());
		assertThat("Field text input not visible", fieldTextInputElement, displayed());

		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
		assertThat("Field text add button not visible", searchPage.fieldTextAddButton().isDisplayed());
		assertThat("Field text input visible", fieldTextInputElement, not(displayed()));
	}

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
		return searchPage.getHeadingResultsCount();
	}

	@Test
	@KnownBug("CSA-1818")
	public void testSearchResultsCount() {
		searchPage.selectLanguage(Language.ENGLISH);
		for (final String query : Arrays.asList("dog", "chips", "dinosaur", "melon", "art")) {
			search(query);
			final int firstPageResultsCount = searchPage.getHeadingResultsCount();

			searchPage.switchResultsPage(Pagination.LAST);
			verifyThat("number of results in title is consistent", searchPage.getHeadingResultsCount(), is(firstPageResultsCount));

			final int completePages = searchPage.getCurrentPageNumber() - 1;
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			final int expectedCount = completePages * SearchPage.RESULTS_PER_PAGE + lastPageDocumentsCount;
			verifyThat("number of results is as expected", searchPage.getHeadingResultsCount(), is(expectedCount));
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
			Waits.loadOrFadeWait();
			assertThat(searchPage.getText(), not(containsString("Terminating boolean operator")));
		}
	}

	@Test
	@KnownBug("IOD-6855")
	public void testFromDateFilter() throws ParseException {
		final Date date = beginDateFilterTest();
		final String firstResult = searchPage.getSearchResult(1).getTitleString();
		final Date invalidDate = DateUtils.addMinutes(date, 1);

		searchPage.filterBy(new DatePickerFilter().from(date));
		for (final String label : searchPage.filterLabelList()) {
			assertThat("no 'Until' filter applied", label,not(containsString("Until: ")));
		}
		assertThat("applied 'From' filter", searchPage.fromDateInput().getValue(), not(isEmptyOrNullString()));
		verifyValidDate(firstResult);

		searchPage.filterBy(new DatePickerFilter().from(invalidDate));
		verifyInvalidDate(firstResult);

		searchPage.filterBy(new DatePickerFilter().from(date));
		verifyValidDate(firstResult);
	}

	@Test
	@KnownBug("IOD-6855")
	public void testUntilDateFilter() throws ParseException {
		final Date date = beginDateFilterTest();
		final String firstResult = searchPage.getSearchResult(1).getTitleString();

		// plus 1 minute to be inclusive
		final Date validDate = DateUtils.addMinutes(date, 1);

		searchPage.filterBy(new DatePickerFilter().until(validDate));
		for (final String label : searchPage.filterLabelList()) {
			assertThat("no 'From' filter applied", label,not(containsString("From: ")));
		}
		assertThat("applied 'Until' filter", searchPage.untilDateInput().getValue(), not(isEmptyOrNullString()));
		verifyValidDate(firstResult);

		searchPage.filterBy(new DatePickerFilter().until(date));
        verifyInvalidDate(firstResult);

		searchPage.filterBy(new DatePickerFilter().until(validDate));
		verifyValidDate(firstResult);
	}

	private Date beginDateFilterTest() {
		// not all indexes have times configured
		search(new SearchQuery("Dog").withFilter(new IndexFilter("news_eng")));
		Date date = searchPage.getSearchResult(1).getDate();
		if (date == null) {
			throw new IllegalStateException("date filter test requires first search result to have a date");
		}
		logger.info("First Result: " + searchPage.getSearchResult(1).getTitleString() + " " + date);
		return date;
	}

	private void verifyValidDate(String firstResult) {
		logger.info("from: " + searchPage.fromDateInput().getValue());
		logger.info("until: " + searchPage.untilDateInput().getValue());
		if (verifyThat(searchPage.getHeadingResultsCount(), greaterThan(0))) {
			verifyThat("Document should be displayed again", searchPage.getSearchResult(1).getTitleString(), is(firstResult));
		}
	}

	private void verifyInvalidDate(String firstResult) {
		logger.info("from: " + searchPage.fromDateInput().getValue());
		logger.info("until: " + searchPage.untilDateInput().getValue());
		if (searchPage.getHeadingResultsCount() > 0) {
			verifyThat("Document should not be displayed", searchPage.getSearchResult(1).getTitleString(), not(firstResult));
		}
	}

	@Test
	public void testFromDateAlwaysBeforeUntilDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, Calendar.MAY, 4, 12, 0);
		final Date date = calendar.getTime();

		searchPage.filterBy(new StringDateFilter().from(date).until(date));
		assertThat("Dates should be equal", searchPage.fromDateInput().getValue(), is(searchPage.untilDateInput().getValue()));

		searchPage.filterBy(new StringDateFilter().from(DateUtils.addMinutes(date, 1)).until(date));
		searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        assertThat("From date should be blank", searchPage.fromDateInput().getValue(), isEmptyOrNullString());

		searchPage.filterBy(new StringDateFilter().from(date).until(DateUtils.addMinutes(date, -1)));
		searchPage.sortBy(SearchBase.Sort.RELEVANCE);
        assertThat("Until date should be blank", searchPage.untilDateInput().getValue(), isEmptyOrNullString());
	}

	@Test
	public void testFromDateEqualsUntilDate() throws ParseException {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2012, Calendar.DECEMBER, 12, 12, 12);
		final Date date = calendar.getTime();

		searchPage.filterBy(new StringDateFilter().from(date).until(date));

		assertThat(searchPage.fromDateInput().getValue(), is(searchPage.untilDateInput().getValue()));

		Date nextDate = DateUtils.addMinutes(date, 1);
		searchPage.filterBy(new StringDateFilter().until(nextDate));
		assertThat(searchPage.untilDateInput().getValue(), is(searchPage.formatInputDate(nextDate)));

		nextDate = DateUtils.addMinutes(date, -1);
		searchPage.filterBy(new StringDateFilter().until(nextDate));
        assertThat(searchPage.untilDateInput().getValue(), isEmptyOrNullString());
	}

	@Test
	public void testSortByRelevance() {
		search("string");
		searchPage.sortBy(SearchBase.Sort.RELEVANCE);
		List<Float> weights = searchPage.getWeightsOnPage(5);

        logger.info("Weight of 0: " + weights.get(0));

        for (int i = 0; i < weights.size() - 1; i++) {
            logger.info("Weight of " + (i + 1) + ": " + weights.get(i + 1));

			assertThat("Weight of search result " + i + " is not greater that weight of search result " + (i + 1), weights.get(i), greaterThanOrEqualTo(weights.get(i + 1)));
		}

		searchPage.sortBy(SearchBase.Sort.DATE);
		searchPage.sortBy(SearchBase.Sort.RELEVANCE);
		weights = searchPage.getWeightsOnPage(5);
		for (int i = 0; i < weights.size() - 1; i++) {
			assertThat("Weight of search result " + i + " is not greater that weight of search result " + (i + 1), weights.get(i), greaterThanOrEqualTo(weights.get(i + 1)));
		}

		searchPage.sortBy(SearchBase.Sort.DATE);
		search("paper packages");
		searchPage.sortBy(SearchBase.Sort.RELEVANCE);
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
		getApplication().switchTo(PromotionsPage.class);

		getDriver().navigate().refresh();
		final String newSearchText = getElementFactory().getTopNavBar().getSearchBarText();
		assertThat("search bar should be blank on refresh of a page that isn't the search page", newSearchText, is(searchText));
	}

	@Test
	public void testRelatedConceptsLinks() {
		String queryText = "elephant";
		search(queryText);
		assertThat(topNavBar.getSearchBarText(), is(queryText));
		assertThat(searchPage.youSearchedFor(), hasItem(queryText));
		assertThat(searchPage.getHeadingSearchTerm(), containsString(queryText));

		for (int i = 0; i < 5; i++) {
			searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
			searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
			final int conceptsCount = searchPage.countRelatedConcepts();
			assertThat(conceptsCount, lessThanOrEqualTo(50));
			final int index = new Random().nextInt(conceptsCount);
			queryText = searchPage.relatedConcepts().get(index).getText();
			searchPage.relatedConcept(queryText).click();
			searchPage.waitForSearchLoadIndicatorToDisappear();

			assertThat(topNavBar.getSearchBarText(), is(queryText));
			List<String> words = new ArrayList<>();
			// HACK: avoid stopwords
			for (String word : queryText.split("\\s+")) {
				if (word.length() > 3) {
					words.add(word);
				}
			}
			assertThat(searchPage.youSearchedFor(), containsItems(words));
			assertThat(searchPage.getHeadingSearchTerm(), containsString(queryText));
		}
	}

	@Test
	public void testRelatedConceptsDifferentInDifferentLanguages() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), Matchers.not(ApplicationType.HOSTED));

		search("France");
		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> englishConcepts = ElementUtil.webElementListToStringList(searchPage.relatedConcepts());
		searchPage.selectLanguage(Language.FRENCH);
		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> frenchConcepts = ElementUtil.webElementListToStringList(searchPage.relatedConcepts());

		assertThat("Concepts should be different in different languages", englishConcepts, not(containsInAnyOrder(frenchConcepts.toArray())));

		searchPage.selectLanguage(Language.ENGLISH);
		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
		searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
		final List<String> secondEnglishConcepts = ElementUtil.webElementListToStringList(searchPage.relatedConcepts());
		assertThat("Related concepts have changed on second search of same query text", englishConcepts, contains(secondEnglishConcepts.toArray()));
	}

	@Test
	@KnownBug("CSA-1819")
	public void testNavigateToLastPageOfSearchResultsAndEditUrlToTryAndNavigateFurther() {
        search("nice");
		searchPage.switchResultsPage(Pagination.LAST);
		final int currentPage = searchPage.getCurrentPageNumber();
		final String docTitle = searchPage.getSearchResult(1).getTitleString();
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
		assertThat("Search results have changed on last page", docTitle, is(searchPage.getSearchResult(1).getTitleString()));
	}

	@Test
	public void testNoRelatedConceptsIfNoResultsFound() {
		final String garbageQueryText = "garbagedjlsfjijlsf";
		search(garbageQueryText);

        String errorMessage = "Garbage text returned results. garbageQueryText string needs changed to be more garbage like";
		assertThat(errorMessage, searchPage.getText(), containsString("No results found"));
		assertThat(errorMessage, searchPage.getHeadingResultsCount(), is(0));

		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        assertThat("If there are no search results there should be no related concepts", searchPage.getText(), containsString("No related concepts found"));
	}

	@Test
	public void testParametricValuesLoads() throws InterruptedException {
		searchPage.expand(SearchBase.Facet.FILTER_BY);
		searchPage.expand(SearchBase.Facet.PARAMETRIC_VALUES);
		Thread.sleep(20000);
		assertThat("Load indicator still visible after 20 seconds", searchPage.parametricValueLoadIndicator().isDisplayed(), is(false));
	}

	@Test
	public void testFilteringByParametricValues(){
		search("Alexis");

		searchPage.openParametricValuesList();
		searchPage.waitForParametricValuesToLoad();

		//Need to get the result BEFORE filtering, and check that it's the same as after
		int expectedResults = plainTextCheckbox().getResultsCount();
		plainTextCheckbox().check();
		goToLastPage();
		verifyResultCounts(plainTextCheckbox(), expectedResults);
		verifyTicks(true, false);

		expectedResults = plainTextCheckbox().getResultsCount();
		simpsonsArchiveCheckbox().check();
		goToLastPage();
		verifyResultCounts(plainTextCheckbox(), expectedResults);	//TODO Maybe change plainTextCheckbox to whichever has the higher value??
		verifyTicks(true, true);

		plainTextCheckbox().uncheck();
		goToLastPage();
		//Get this after unfiltering so it's accurate.
		expectedResults = simpsonsArchiveCheckbox().getResultsCount();
		verifyResultCounts(simpsonsArchiveCheckbox(), expectedResults);
		verifyTicks(false, true);
	}

	private void verifyResultCounts(SOCheckbox checked, int expectedResults){
		int resultsTotal = ((searchPage.getCurrentPageNumber() - 1) * SearchPage.RESULTS_PER_PAGE) + searchPage.visibleDocumentsCount();
		int checkboxResults = checked.getResultsCount();

		verifyThat(searchPage.getHeadingResultsCount(), is(expectedResults));
		verifyThat(resultsTotal, is(expectedResults));
		verifyThat(checkboxResults, is(expectedResults));
	}

	private void verifyTicks(boolean plainChecked, boolean simpsonsChecked) {
		verifyThat(plainTextCheckbox().isChecked(), is(plainChecked));
		verifyThat(simpsonsArchiveCheckbox().isChecked(), is(simpsonsChecked));
	}

	private void goToLastPage(){
		try {
			Waits.loadOrFadeWait();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			searchPage.switchResultsPage(Pagination.LAST);
		} catch (WebDriverException e) {
			/* Already on last page */
		}
	}

	private SOCheckbox simpsonsArchiveCheckbox(){
		return searchPage.parametricTypeCheckbox("Source Connector", "SIMPSONSARCHIVE");
	}

	private SOCheckbox plainTextCheckbox(){
		return searchPage.parametricTypeCheckbox("Content Type", "TEXT/PLAIN");
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
			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	@Test
	@KnownBug("CSA-1708")
	public void testParametricLabelsNotUndefined(){
		searchService.search(new SearchQuery("simpsons")
				.withFilter(new IndexFilter(Index.DEFAULT))
				.withFilter(new ParametricFilter("Content Type", "TEXT/HTML")));

		for(WebElement filter : searchPage.findElements(By.cssSelector(".filter-display-view span"))){
			assertThat(filter.getText().toLowerCase(),not(containsString("undefined")));
		}
	}

	@Test
	@KnownBug("CSA-1629")
	public void testPinToPositionPagination(){
		PromotionService promotionService = getApplication().promotionService();

		try {
			promotionService.setUpPromotion(new PinToPositionPromotion(1, "thiswillhavenoresults"), "*", SearchPage.RESULTS_PER_PAGE + 2);
			searchPage.waitForSearchLoadIndicatorToDisappear();

			verifyThat(searchPage.resultsPaginationButton(Pagination.NEXT), not(disabled()));
			searchPage.switchResultsPage(Pagination.NEXT);

			verifyThat(searchPage.visibleDocumentsCount(), is(2));
		} finally {
			promotionService.deleteAll();
		}
	}

	@Test
	public void testDeletingDocument(){
		searchService.search("bbc");

		//Hopefully less important documents will be on the last page
		searchPage.switchResultsPage(Pagination.LAST);

		int results = searchPage.getHeadingResultsCount();
		String deletedDoc = searchPage.getSearchResult(1).getTitleString();

		// Might wanna check this doesn't come up --- hp-icon hp-trash hp-lg fa-spin fa-circle-o-notch
		searchService.deleteDocument(deletedDoc);

		verifyThat(searchPage.getHeadingResultsCount(), is(--results));
		verifyThat(searchPage.getSearchResult(1).getTitleString(), not(is(deletedDoc)));
	}

	@Test
	public void testIndexSelection() {
		Index firstIndex;
		Index secondIndex;
		if (config.getType().equals(ApplicationType.ON_PREM)) {
			firstIndex = new Index("wikienglish");
			secondIndex = new Index("wookiepedia");
		} else {
			firstIndex = new Index("news_eng");
			secondIndex = new Index("news_ger");
		}

		searchService.search(new SearchQuery("car").withFilter(new LanguageFilter(Language.ENGLISH)).withFilter(IndexFilter.ALL));
		IndexesTree indexesTree = searchPage.indexesTree();

		for (IndexNodeElement node : indexesTree) {
			assertThat(node.getName() + " is selected", node.isSelected(), is(true));
		}
		assertThat("all indexes selected", indexesTree.allIndexes().isSelected(), is(true));

		searchPage.filterBy(new IndexFilter(firstIndex));
		assertThat("all indexes checkbox not selected", indexesTree.allIndexes().isSelected(), is(false));
		assertThat("only one index should be selected", indexesTree.getSelected(), hasSize(1));
		assertThat("correct index selected", indexesTree.getSelected(), hasItem(firstIndex));
		final String firstIndexResult = searchPage.getSearchResult(1).getTitleString();

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertThat("result " + i + " from " + firstIndex, searchPage.getSearchResult(i).getIndex(), is(firstIndex));
			}
			searchPage.switchResultsPage(Pagination.NEXT);
		}

		searchPage.switchResultsPage(Pagination.FIRST);
		indexesTree.select(secondIndex);
		indexesTree.deselect(firstIndex);
		assertThat("only one index should be selected", indexesTree.getSelected(), hasSize(1));
		assertThat("correct index selected", indexesTree.getSelected(), hasItem(secondIndex));
		final String secondIndexResult = searchPage.getSearchResult(1).getTitleString();
		assertThat(secondIndexResult, not(firstIndexResult));

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertThat("result " + i + " from " + secondIndex, searchPage.getSearchResult(i).getIndex(), is(secondIndex));
			}
			searchPage.switchResultsPage(Pagination.NEXT);
		}

		searchPage.switchResultsPage(Pagination.FIRST);
		indexesTree.select(firstIndex);
		assertThat("2 indexes should be selected", indexesTree.getSelected(), hasSize(2));
		assertThat("correct indexes selected", indexesTree.getSelected(), hasItems(firstIndex, secondIndex));
		assertThat("search result from selected indexes", searchPage.getSearchResult(1).getTitleString(), isOneOf(firstIndexResult, secondIndexResult));

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertThat("result " + i + " from either index", searchPage.getSearchResult(i).getIndex(), anyOf(is(firstIndex), is(secondIndex)));
			}
			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	@Test
	@KnownBug("CSA-2061")
	public void testHeadingCount(){
		searchService.search(new SearchQuery("dog").withFilter(IndexFilter.ALL));

		verifyThat(searchPage.getHeadingResultsCount(), lessThanOrEqualTo(2501));
	}

	@Test
	@KnownBug("CSA-2060")
	public void testResultIndex(){
		searchService.search(new SearchQuery("Jamaica"));

		for(SOSearchResult searchResult : searchPage.getSearchResults()){
			verifyThat(searchResult.getIndex().getDisplayName(), not(containsString("Object")));
		}
	}
}
