package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.control.Frame;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.element.Pagination;
import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SOSearchResult;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ControlMatchers.url;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;
import static com.autonomy.abc.matchers.ElementMatchers.checked;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
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

	@Test
	public void testSearchHeading(){
		List<String> terms = Arrays.asList("dog", "cat", "ElEPhanT");
		for (String term : terms) {
			search(term);
			assertThat(searchPage.getHeadingSearchTerm(), is(term));
		}
	}

	@Test
	public void testPromoteButton(){
		searchPage.openPromotionsBucket();
		checkBucketEmpty();

		searchPage.addDocToBucket(1);
		assertThat(searchPage.promoteTheseItemsButton(), not(disabled()));
		assertThat(searchPage.promotedItemsCount(), is(1));

		searchPage.closePromotionsBucket();
		assertThat(searchPage.promotionsBucket(), not(displayed()));

		searchPage.openPromotionsBucket();
		checkBucketEmpty();
	}

	private void checkBucketEmpty() {
		assertThat(searchPage.promotionsBucket(), displayed());
		assertThat(searchPage.promoteTheseItemsButton(), disabled());
		assertThat(searchPage.promotedItemsCount(), is(0));
	}

    @Test
	public void testUnmodifiedResultsToggleButton(){
        assertThat(searchPage.modifiedResults(), checked());
		assertThat(getWindow(), urlContains("/modified"));

		searchPage.modifiedResults().uncheck();
        assertThat(searchPage.modifiedResults(), not(checked()));
		assertThat(getWindow(), urlContains("/unmodified"));

		searchPage.modifiedResults().check();
        assertThat(searchPage.modifiedResults(), checked());
        assertThat(getWindow(), urlContains("/modified"));
	}

	@Test
	public void testAddFilesToPromoteBucket() {
		searchPage.openPromotionsBucket();

		for (int i = 1; i < 7; i++) {
			searchPage.addDocToBucket(i);
			assertThat("Promoted items count not correct", searchPage.promotedItemsCount(),is(i));
		}

		for (int j = 6; j > 0; j--) {
			searchPage.removeDocFromBucket(j);
			assertThat("Promoted items count not correct", searchPage.promotedItemsCount(), is(j - 1));
		}

		searchPage.closePromotionsBucket();
	}

	@Test
	public void testAddDocumentToPromotionsBucket() {
		search("horse");
		searchPage.openPromotionsBucket();
		searchPage.addDocToBucket(1);
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(1));
		assertThat("File in bucket description does not match file added", searchPage.getSearchResult(1).getTitleString(), equalToIgnoringCase(searchPage.bucketDocumentTitle(1)));
	}

	@Test
	public void testPromoteTheseItemsButtonLink() {
		search("fox");
		searchPage.openPromotionsBucket();
		searchPage.addDocToBucket(1);
		searchPage.promoteTheseItemsButton().click();
		assertThat(getWindow(), url(endsWith("promotions/create")));
	}

	@Test
	public void testDocumentsRemainInBucket() {
		search("cow");
		searchPage.openPromotionsBucket();
		searchPage.addDocsToBucket(2);
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount(), is(2));

		search("bull");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount(), is(2));
		searchPage.addDocToBucket(1);
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(3));

		search("cow");
		assertThat("Promoted items count should equal 2", searchPage.promotedItemsCount(), is(3));

		search("bull");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(3));

		searchPage.removeDocFromBucket(1);
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(2));

		search("cow");
		assertThat("Promoted items count should equal 1", searchPage.promotedItemsCount(), is(2));
	}

	@Test
	//TODO seems to be failing within VM - investigate futher
	public void testDeleteDocsFromWithinBucket() {
		search("sabre");
		searchPage.openPromotionsBucket();
		searchPage.addDocsToBucket(4);

		final List<String> bucketList = searchPage.promotionsBucketList();
		assertThat("There should be four documents in the bucket", bucketList.size(), is(4));
		assertThat(searchPage.promoteTheseDocumentsButton(), disabled());
		assertThat(searchPage.promoteTheseItemsButton(), displayed());

		searchPage.emptyBucket();

		assertThat("promote button should be disabled when bucket has no documents", searchPage.promoteTheseItemsButton(), disabled());

		search("tooth");
		assertThat("Wrong number of documents in the bucket", searchPage.promotionsBucketList(), empty());

		searchPage.addDocToBucket(5);
		final List<String> docTitles = new ArrayList<>();
		docTitles.add(searchPage.getSearchResult(5).getTitleString());
		searchPage.switchResultsPage(Pagination.NEXT);
		searchPage.addDocToBucket(3);
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

		for (int page = 1; page <= 2; page++) {
			for (int result = 1; result <= 6; result++) {
				Waits.loadOrFadeWait();
				searchPage.getSearchResult(1).icon().click();
				checkViewResult();
				searchPage.getSearchResult(1).title().click();
				checkViewResult();
			}

			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	@Test
	public void testViewFromBucketLabel() throws InterruptedException {
		search("Engineer");
		searchPage.openPromotionsBucket();

		for (int j = 1; j <=2; j++) {
			for (int i = 1; i <= 3; i++) {
				searchPage.addDocToBucket(i);
				final String docTitle = searchPage.getSearchResult(i).getTitleString();
				ElementUtil.scrollIntoViewAndClick(searchPage.promotionBucketElementByTitle(docTitle), getDriver());
				checkViewResult();
			}

			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	// TODO: after CCUK-3728 use SharedPreviewTests
	@RelatedTo("CCUK-3728")
	private void checkViewResult() {
		DocumentViewer docViewer = DocumentViewer.make(getDriver());
		Frame frame = new Frame(getWindow(), docViewer.frame());

		verifyThat(frame.getText(), not(isEmptyOrNullString()));
		docViewer.close();

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

		getWindow().refresh();
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
					verifyThat(searchElement.getText(), containsString(searchTerm));
				}
				verifyThat(searchElement.getTagName(), is("a"));
				verifyThat(searchElement.getAttribute("class"), is("query-text"));

				WebElement parent = searchElement.findElement(By.xpath(".//.."));
				verifyThat(parent.getTagName(), is("span"));
				verifyThat(parent.getAttribute("class"), containsString("label"));
			}
			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	@Test
	@KnownBug("CSA-1708")
	public void testParametricLabelsNotUndefined(){
		searchService.search(new Query("simpsons")
				.withFilter(new IndexFilter(Index.DEFAULT))
				.withFilter(new ParametricFilter("Content Type", "TEXT/HTML")));

		for(WebElement filter : searchPage.findElements(By.cssSelector(".filter-display-view span"))){
			assertThat(filter.getText().toLowerCase(),not(containsString("undefined")));
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
		verifyThat(searchPage.getSearchResult(1).getTitleString(), is(not(deletedDoc)));
	}

	@Test
	public void testIndexSelection() {
		Index firstIndex;
		Index secondIndex;
		if (isOnPrem()) {
			firstIndex = new Index("wikienglish");
			secondIndex = new Index("wookiepedia");
		} else {
			firstIndex = new Index("news_eng");
			secondIndex = new Index("news_ger");
		}

		searchService.search(new Query("car").withFilter(new LanguageFilter(Language.ENGLISH)).withFilter(IndexFilter.ALL));
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
		searchService.search(new Query("dog").withFilter(IndexFilter.ALL));

		verifyThat(searchPage.getHeadingResultsCount(), lessThanOrEqualTo(2501));
	}

	@Test
	@KnownBug("CSA-2060")
	public void testResultIndex(){
		searchService.search(new Query("Jamaica"));

		for(SOSearchResult searchResult : searchPage.getSearchResults()){
			verifyThat(searchResult.getIndex().getDisplayName(), not(containsString("Object")));
		}
	}
}
