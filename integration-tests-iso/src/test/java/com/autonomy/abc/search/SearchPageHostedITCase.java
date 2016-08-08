package com.autonomy.abc.search;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.FieldTextFilter;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPageHostedITCase extends IsoHsodTestBase {
	private SearchPage searchPage;
	private SearchService searchService;

	public SearchPageHostedITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void setUp() {
		searchService = getApplication().searchService();
		searchPage = searchService.search("example");
	}

	@Ignore("TODO: Not implemented")
	@Test
	public void testParametricSearch() {
		searchService.search(new Query("*").withFilter(IndexFilter.ALL));
	}

	@Test
	public void testFieldTextFilter() {
		searchService.search(new Query("Harrison Ford").withFilter(IndexFilter.WIKI_ENG));

		searchPage.expand(SearchBase.Facet.FIELD_TEXT);
		searchPage.fieldTextAddButton().click();
		Waits.loadOrFadeWait();
		assertThat("input visible", searchPage.fieldTextInput().getElement(), displayed());
		assertThat("confirm button visible", searchPage.fieldTextTickConfirm(), displayed());

		searchPage.filterBy(new FieldTextFilter("MATCH{Actor / Actress}:person_profession"));
		assertThat(searchPage, not(containsText(Errors.Search.HOD)));

		assertThat("edit button visible", searchPage.fieldTextEditButton(), displayed());
		assertThat("remove button visible", searchPage.fieldTextRemoveButton(), displayed());

		final List<String> fieldTextResults = searchPage.getSearchResultTitles(SearchPage.RESULTS_PER_PAGE);

		searchPage.fieldTextRemoveButton().click();
		searchPage.waitForSearchLoadIndicatorToDisappear();

		searchPage.filterBy(new ParametricFilter("Person Profession", "Actor / Actress"));

		verifyThat(searchPage.getSearchResultTitles(SearchPage.RESULTS_PER_PAGE), is(fieldTextResults));
	}

	@Test
	public void testEditFieldText() {
		assumeThat(getAppUrl(), not("http://search.havenapps.io/searchoptimizer/p"));

		searchService.search(new Query("*")
				.withFilter(IndexFilter.PUBLIC)
				.withFilter(new FieldTextFilter("EXISTS{}:place_population")));

		verifyResults("wiki");

		searchPage.filterBy(new FieldTextFilter("EXISTS{}:place_elevation"));
		verifyResults("transport");
	}

	private void verifyResults(final String index){
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
		assertThat(searchPage.getText(), not(containsString(Errors.Search.NO_RESULTS)));
		searchPage.getSearchResult(1).title().click();
		final DocumentViewer documentViewer = DocumentViewer.make(getDriver());
		for(int i = 0; i < SearchPage.RESULTS_PER_PAGE; i++){
			verifyThat(documentViewer.getIndex().getDisplayName(), containsString(index));
			documentViewer.next();
		}
		documentViewer.close();
	}


	@Test
	public void testAuthor(){
		final String author = "FIFA.com";
		searchPage = searchService.search(new Query("blatter").withFilter(new ParametricFilter("Author", author)));

		searchPage.getSearchResult(1).title().click();
		final DocumentViewer documentViewer = DocumentViewer.make(getDriver());

		for(int i = 0; i < SearchPage.RESULTS_PER_PAGE; i++){
			verifyThat(documentViewer.getAuthor(), equalToIgnoringCase(author));
			documentViewer.next();
		}

		documentViewer.close();
	}

	@Test
	@RelatedTo({"CSA-946", "CSA-1656", "CSA-1657", "CSA-1908"})
	public void testDocumentPreview(){
		final Index index = new Index("fifa");
		searchService.search(new Query("document preview").withFilter(new IndexFilter(index)));

		SharedPreviewTests.testDocumentPreviews(getMainSession(), searchPage.getSearchResults().subList(0, 5), index);
	}
}
