package com.autonomy.abc.search;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.element.SOCheckbox;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.query.*;
import com.autonomy.abc.selenium.search.IsoSearchResult;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.*;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsIgnoringCase;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPageITCase extends HybridIsoTestBase {
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

	private void search(final String searchTerm){
		logger.info("Searching for: '" + searchTerm + "'");
		searchPage = searchService.search(searchTerm);
	}

	@Test
	public void testSearchHeading(){
		final List<String> terms = Arrays.asList("dog", "cat", "ElEPhanT");
		for (final String term : terms) {
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
		assertThat(searchPage.getBucketTitles(), hasSize(1));

		searchPage.closePromotionsBucket();
		assertThat(searchPage.promotionsBucket(), not(displayed()));

		searchPage.openPromotionsBucket();
		checkBucketEmpty();
	}

	private void checkBucketEmpty() {
		assertThat(searchPage.promotionsBucket(), displayed());
		assertThat(searchPage.promoteTheseItemsButton(), disabled());
		assertThat(searchPage.getBucketTitles(), empty());
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
			assertThat(searchPage.getBucketTitles(), hasSize(i));
		}

		for (int j = 6; j > 0; j--) {
			searchPage.removeDocFromBucket(j);
			assertThat(searchPage.getBucketTitles(), hasSize(j - 1));
		}

		searchPage.closePromotionsBucket();
	}

	@Test
	public void testAddDocumentToPromotionsBucket() {
		search("horse");
		searchPage.openPromotionsBucket();
		searchPage.addDocToBucket(1);
		assertThat(searchPage.getBucketTitles(), hasSize(1));
		assertThat(searchPage.getBucketTitles(), hasItem(equalToIgnoringCase(searchPage.getSearchResult(1).getTitleString())));
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
		int bucketCount = 2;
		search("cow");
		searchPage.openPromotionsBucket();
		searchPage.addDocsToBucket(bucketCount);
		assertThat(searchPage.getBucketTitles(), hasSize(bucketCount));

		search("bull");
		assertThat(searchPage.getBucketTitles(), hasSize(bucketCount));
		searchPage.addDocToBucket(1);
		assertThat(searchPage.getBucketTitles(), hasSize(++bucketCount));

		search("cow");
		assertThat(searchPage.getBucketTitles(), hasSize(bucketCount));

		search("bull");
		assertThat(searchPage.getBucketTitles(), hasSize(bucketCount));

		searchPage.removeDocFromBucket(1);
		assertThat(searchPage.getBucketTitles(), hasSize(--bucketCount));

		search("cow");
		assertThat(searchPage.getBucketTitles(), hasSize(bucketCount));
	}

	@Test
	//TODO seems to be failing within VM - investigate futher
	public void testDeleteDocsFromWithinBucket() {
		search("sabre");
		searchPage.openPromotionsBucket();
		searchPage.addDocsToBucket(4);

		final List<String> bucketList = searchPage.getBucketTitles();
		assertThat(bucketList, hasSize(4));
		assertThat(searchPage.promoteTheseDocumentsButton(), disabled());
		assertThat(searchPage.promoteTheseItemsButton(), not(disabled()));

		searchPage.emptyBucket();
		assertThat(searchPage.promoteTheseItemsButton(), disabled());

		search("tooth");
		assertThat(searchPage.getBucketTitles(), empty());

		final int doc1Index = 5;
		final String doc1 = searchPage.getSearchResult(5).getTitleString();
		searchPage.addDocToBucket(doc1Index);

		searchPage.switchResultsPage(Pagination.NEXT);
		final int doc2Index = 3;
		final String doc2 = searchPage.getSearchResult(3).getTitleString();
		searchPage.addDocToBucket(doc2Index);

		final List<String> bucketDocs = searchPage.getBucketTitles();
		assertThat(bucketDocs, hasSize(2));
		assertThat(bucketDocs, hasItem(equalToIgnoringCase(doc1)));
		assertThat(bucketDocs, hasItem(equalToIgnoringCase(doc2)));

		verifyRemovingFirstItem(doc2, doc2Index);
		assertThat(searchPage.getBucketTitles(), hasItem(equalToIgnoringCase(doc1)));
		searchPage.switchResultsPage(Pagination.PREVIOUS);
		verifyRemovingFinalItem(doc1, doc1Index);
	}

	private void verifyRemovingFirstItem(final String toRemove, final int number) {
		searchPage.deleteDocFromWithinBucket(toRemove);
		assertThat(searchPage.getBucketTitles(), hasSize(1));
		assertThat(searchPage.getBucketTitles(), not(hasItem(equalToIgnoringCase(toRemove))));
		assertThat(searchPage.searchResultCheckbox(number), not(checked()));
	}

	private void verifyRemovingFinalItem(final String toRemove, final int number) {
		searchPage.deleteDocFromWithinBucket(toRemove);
		assertThat(searchPage.getBucketTitles(), empty());
		assertThat(searchPage.searchResultCheckbox(number), not(checked()));
		assertThat(searchPage.promoteTheseItemsButton(), disabled());
	}

	@Test
	public void testViewFrame() throws InterruptedException {
		search("army");

		for (int page = 1; page <= 2; page++) {
			for (int result = 1; result <= 6; result++) {
				Waits.loadOrFadeWait();
				searchPage.getSearchResult(result).icon().click();
				checkViewResult();
				searchPage.getSearchResult(result).title().click();
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
				DriverUtil.scrollIntoViewAndClick(getDriver(), searchPage.promotionBucketElementByTitle(docTitle));
				checkViewResult();
			}

			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	// TODO: after CCUK-3728 use SharedPreviewTests
	@RelatedTo("CCUK-3728")
	private void checkViewResult() {
		final DocumentViewer docViewer = DocumentViewer.make(getDriver());
		final Frame frame = new Frame(getWindow(), docViewer.frame());

		verifyThat(frame.getText(), not(isEmptyOrNullString()));
		docViewer.close();

	}
    @Test
	public void testFieldTextInputDisappearsOnOutsideClick() {
		searchPage.expand(SearchBase.Facet.FIELD_TEXT);
		assertThat(searchPage.fieldTextAddButton(), displayed());

		final WebElement fieldTextInputElement = searchPage.fieldTextInput().getElement();

		searchPage.fieldTextAddButton().click();
		assertThat(searchPage.fieldTextAddButton(), not(displayed()));
		assertThat(fieldTextInputElement, displayed());

		searchPage.fieldTextInput().getElement().click();
		assertThat(searchPage.fieldTextAddButton(), not(displayed()));
		assertThat(fieldTextInputElement, displayed());

		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
		assertThat(searchPage.fieldTextAddButton(), displayed());
		assertThat(fieldTextInputElement, not(displayed()));
	}

	@Test
	public void testIdolSearchTypes() {

		final int pickupCount = getResultCount("pickup");
		final int truckCount = getResultCount("truck");
		final int unquotedCount = getResultCount("pickup truck");
		final int quotedCount = getResultCount("\"pickup truck\"");
		final int orCount = getResultCount("pickup OR truck");
		final int andCount = getResultCount("pickup AND truck");
		final int pickupNotTruckCount = getResultCount("pickup NOT truck");
		final int truckNotPickupCount = getResultCount("truck NOT pickup");

		verifyThat(pickupCount, lessThanOrEqualTo(unquotedCount));
        verifyThat(quotedCount, lessThanOrEqualTo(unquotedCount));

		verifyThat(pickupNotTruckCount, lessThanOrEqualTo(pickupCount));
		verifyThat(truckNotPickupCount, lessThanOrEqualTo(truckCount));

		verifyThat(quotedCount, lessThanOrEqualTo(andCount));
		verifyThat(andCount, lessThanOrEqualTo(unquotedCount));

		verifyThat(orCount, lessThanOrEqualTo(pickupCount + truckCount));
		verifyThat(andCount + pickupNotTruckCount + truckNotPickupCount, is(orCount));
		verifyThat(orCount, is(unquotedCount));
	}

	private int getResultCount(final String searchTerm) {
		search(searchTerm);
		return searchPage.getHeadingResultsCount();
	}

	@Test
	@ResolvedBug("CSA-1818")
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
			verifyThat(searchPage.getHeadingResultsCount(), is(expectedCount));
		}
	}

	@Test
	public void testSortByRelevance() {
		search("string");
		searchPage.sortBy(SortBy.RELEVANCE);
		checkWeightsForPages(5);

		searchPage.sortBy(SortBy.DATE);
		searchPage.sortBy(SortBy.RELEVANCE);
		checkWeightsForPages(5);

		searchPage.sortBy(SortBy.DATE);
		search("paper packages");
		searchPage.sortBy(SortBy.RELEVANCE);
		checkWeightsForPages(5);
	}

	private void checkWeightsForPages(final int numberOfPages) {
		final List<Float> weights = searchPage.getWeightsOnPage(numberOfPages);
		final List<Float> sortedWeights = new ArrayList<>(weights);
		Collections.sort(sortedWeights, Collections.reverseOrder());
		assertThat(sortedWeights, equalTo(weights));
	}

	@Test
	public void testSearchBarTextPersistsOnRefresh() {
		final String searchText = "Stay";
		search(searchText);

		// Change to promotions page since the search page will persist the query in the URL
		getApplication().switchTo(PromotionsPage.class);

		getWindow().refresh();
		final String newSearchText = getElementFactory().getTopNavBar().getSearchBarText();
		assertThat(newSearchText, is(searchText));
	}

	@Test
	@ResolvedBug("ISO-38")
	public void testRelatedConceptsLinks() {
		String queryText = "elephant";
		search(queryText);
		assertThat(topNavBar.getSearchBarText(), is(queryText));
		assertThat(searchPage.youSearchedFor(), hasItem(queryText));
		assertThat(searchPage.getHeadingSearchTerm(), containsString(queryText));

		for (int i = 0; i < 5; i++) {
			searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
			searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
			final int conceptsCount = searchPage.relatedConcepts().size();
			assertThat(conceptsCount, lessThanOrEqualTo(50));
			final int index = new Random().nextInt(conceptsCount);
			queryText = searchPage.relatedConcepts().get(index).getText();
			searchPage.relatedConcept(queryText).click();
			searchPage.waitForSearchLoadIndicatorToDisappear();

			assertThat(topNavBar.getSearchBarText(), is(queryText));
			final List<String> words = new ArrayList<>();
			// HACK: avoid stopwords
			for (final String word : queryText.split("\\s+")) {
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
		assumeThat(searchPage.errorContainer(), containsText(Errors.Search.NO_RESULTS));

		searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        assertThat(searchPage.getText(), containsString(Errors.Search.NO_CONCEPTS));
	}

	@Test
	public void testParametricValuesLoads() throws InterruptedException {
		searchPage.expand(SearchBase.Facet.FILTER_BY);
		searchPage.expand(SearchBase.Facet.PARAMETRIC_VALUES);
		Thread.sleep(20000);
		assertThat(searchPage.parametricValueLoadIndicator(), not(displayed()));
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

	private void verifyResultCounts(final SOCheckbox checked, final int expectedResults){
		final int resultsTotal = ((searchPage.getCurrentPageNumber() - 1) * SearchPage.RESULTS_PER_PAGE) + searchPage.visibleDocumentsCount();
		final int checkboxResults = checked.getResultsCount();

		verifyThat(searchPage.getHeadingResultsCount(), is(expectedResults));
		verifyThat(resultsTotal, is(expectedResults));
		verifyThat(checkboxResults, is(expectedResults));
	}

	private void verifyTicks(final boolean plainChecked, final boolean simpsonsChecked) {
		verifyThat(plainTextCheckbox().isChecked(), is(plainChecked));
		verifyThat(simpsonsArchiveCheckbox().isChecked(), is(simpsonsChecked));
	}

	private void goToLastPage(){
		try {
			Waits.loadOrFadeWait();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			searchPage.switchResultsPage(Pagination.LAST);
		} catch (final WebDriverException e) {
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
		final String searchTerm = "Tiger";

		search(searchTerm);

		for(int i = 0; i < 3; i++) {
			for (final WebElement searchElement : getDriver().findElements(By.xpath("//div[contains(@class,'search-results-view')]//p//*[contains(text(),'" + searchTerm + "')]"))) {
				if (searchElement.isDisplayed()) {        //They can become hidden if they're too far in the summary
					verifyThat(searchElement.getText(), containsString(searchTerm));
				}
				verifyThat(searchElement.getTagName(), is("a"));
				verifyThat(searchElement.getAttribute("class"), is("query-text"));

				final WebElement parent = searchElement.findElement(By.xpath(".//.."));
				verifyThat(parent.getTagName(), is("span"));
				verifyThat(parent.getAttribute("class"), containsString("label"));
			}
			searchPage.switchResultsPage(Pagination.NEXT);
		}
	}

	@Test
	@ResolvedBug("CSA-1708")
	public void testParametricLabelsNotUndefined(){
		searchService.search(new Query("simpsons").withFilter(new ParametricFilter("Content Type", "TEXT/HTML")));

		for(final WebElement filter : searchPage.findElements(By.cssSelector(".filter-display-view span"))){
			assertThat(filter.getText().toLowerCase(),not(containsString("undefined")));
		}
	}

	@Test
	@ResolvedBug("ISO-40")
	public void testDeletingDocument(){
		searchPage.selectLanguage(Language.ENGLISH);
		searchService.search("face");

		//Hopefully less important documents will be on the last page
		searchPage.switchResultsPage(Pagination.LAST);
		int results = searchPage.getHeadingResultsCount();
		final String deletedDoc = searchPage.getSearchResult(1).getTitleString();

		// Might wanna check this doesn't come up --- hp-icon hp-trash hp-lg fa-spin fa-circle-o-notch
		searchService.deleteDocument(deletedDoc);

		verifyThat(searchPage.getHeadingResultsCount(), is(--results));
		verifyThat(searchPage.getSearchResult(1).getTitleString(), is(not(deletedDoc)));
	}

	@Test
	public void testIndexSelection() {
		final Index firstIndex;
		final Index secondIndex;
		if (isOnPrem()) {
			firstIndex = new Index("WikiEnglish");
			secondIndex = new Index("Wookiepedia");
		} else {
			firstIndex = new Index("news_eng");
			secondIndex = new Index("news_ger");
		}
		final List<Index> selected = new ArrayList<>();

		searchService.search(
				new Query("car")
						.withFilter(new LanguageFilter(Language.ENGLISH))
						.withFilter(IndexFilter.ALL));
		final IndexesTree indexesTree = searchPage.indexesTree();

		assertThat(indexesTree.allIndexes(), selected());
		assertThat(indexesTree, everyItem(selected()));

		indexesTree.allIndexes().deselect();
		selectIndex(firstIndex, selected);
		checkIndexes(selected);
		assertThat(indexesTree.allIndexes(), not(selected()));

		final String firstIndexResult = searchPage.getSearchResult(1).getTitleString();
		checkResultPagesForIndexes(2, selected);

		selectIndex(secondIndex, selected);
		deselectIndex(firstIndex, selected);
		checkIndexes(selected);
		assertThat(indexesTree.getSelected(), not(hasItem(firstIndex)));

		final String secondIndexResult = searchPage.getSearchResult(1).getTitleString();
		assertThat(secondIndexResult, not(firstIndexResult));
		checkResultPagesForIndexes(2, selected);

		selectIndex(firstIndex, selected);
		checkIndexes(selected);
		assertThat(searchPage.getSearchResult(1).getTitleString(),
				isOneOf(firstIndexResult, secondIndexResult));
		checkResultPagesForIndexes(2, selected);
	}

	private Matcher<IndexNodeElement> selected() {
		return new TypeSafeMatcher<IndexNodeElement>() {
			@Override
			protected boolean matchesSafely(final IndexNodeElement indexNodeElement) {
				return indexNodeElement.isSelected();
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("selected");
			}
		};
	}

	private void checkIndexes(final List<Index> selected) {
		final IndexesTree indexesTree = searchPage.indexesTree();
		assertThat(indexesTree.getSelected(), hasSize(selected.size()));
		assertThat(indexesTree.getSelected(), containsItems(selected));
	}

	private void selectIndex(final Index toSelect, final List<Index> selected) {
		searchPage.indexesTree().select(toSelect);
		selected.add(toSelect);
	}

	private void deselectIndex(final Index toDeselect, final List<Index> selected) {
		searchPage.indexesTree().deselect(toDeselect);
		selected.remove(toDeselect);
	}

	private void checkResultPagesForIndexes(final int numberOfPages, final List<Index> indexes) {
		for (int j = 1; j <= numberOfPages; j++) {
			for (int i = 1; i <= SearchPage.RESULTS_PER_PAGE; i++) {
				assertThat("result p" + j + " #" + i + " in " + indexes,
						searchPage.getSearchResult(i).getIndex(), isIn(indexes));
			}
			searchPage.switchResultsPage(Pagination.NEXT);
			searchPage.waitForSearchLoadIndicatorToDisappear();
		}
		searchPage.switchResultsPage(Pagination.FIRST);
	}

	@Test
	@ResolvedBug("ISO-16")
	public void testSelectedIndexesAppearInSausage() {
		searchService.search(new Query("mellow").withFilter(new LanguageFilter(Language.ENGLISH)).withFilter(IndexFilter.ALL));
		final IndexesTree indexesTree = searchPage.indexesTree();

		for (final Index removedIndex : indexesTree.getSelected()) {
			indexesTree.deselect(removedIndex);
			for (final Index index : indexesTree.getSelected()) {
				verifyThat("Database/index sausage contains selected indexes", searchPage.getDatabaseFilterSausage().getText(), containsIgnoringCase(index.getName()));
			}
		}

	}


	@Test
	@ResolvedBug("CSA-2061")
	public void testHeadingCount(){
		searchService.search(new Query("dog").withFilter(IndexFilter.ALL));

		verifyThat(searchPage.getHeadingResultsCount(), lessThanOrEqualTo(2501));
	}

	@Test
	@ResolvedBug("CSA-2060")
	public void testResultIndex(){
		searchService.search(new Query("Jamaica"));

		for(final IsoSearchResult searchResult : searchPage.getSearchResults()){
			verifyThat(searchResult.getIndex().getDisplayName(), not(containsString("Object")));
		}
	}
}
