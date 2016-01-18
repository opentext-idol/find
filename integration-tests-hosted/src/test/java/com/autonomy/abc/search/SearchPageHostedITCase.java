package com.autonomy.abc.search;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.*;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchPageHostedITCase extends HostedTestBase {
	private SearchPage searchPage;
	private SearchService searchService;

	public SearchPageHostedITCase(final TestConfig config) {
		super(config);
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.HOSTED);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
		searchService = getApplication().createSearchService(getElementFactory());
		searchPage = searchService.search("example");
	}

	@Ignore("TODO: Not implemented")
	@Test
	public void testParametricSearch() {
		searchService.search(new SearchQuery("*").withFilter(IndexFilter.ALL));
	}

	@Test
	public void testFieldTextFilter() {
		searchService.search(new SearchQuery("Harrison Ford").withFilter(new IndexFilter("wiki_eng")));

		searchPage.expand(SearchBase.Facet.FIELD_TEXT);
		searchPage.fieldTextAddButton().click();
		Waits.loadOrFadeWait();
		assertThat("input visible", searchPage.fieldTextInput().getElement(), displayed());
		assertThat("confirm button visible", searchPage.fieldTextTickConfirm(), displayed());

		searchPage.filterBy(new FieldTextFilter("MATCH{Actor / Actress}:person_profession"));
		assertThat(searchPage, not(containsText(Errors.Search.HOD)));

		assertThat("edit button visible", searchPage.fieldTextEditButton(), displayed());
		assertThat("remove button visible", searchPage.fieldTextRemoveButton(), displayed());

		List<String> fieldTextResults = searchPage.getSearchResultTitles(SearchPage.RESULTS_PER_PAGE);

		searchPage.fieldTextRemoveButton().click();
		searchPage.waitForSearchLoadIndicatorToDisappear();

		searchPage.filterBy(new ParametricFilter("Person Profession", "Actor / Actress"));

		verifyThat(searchPage.getSearchResultTitles(SearchPage.RESULTS_PER_PAGE), is(fieldTextResults));
	}

	@Test
	public void testEditFieldText() {
		searchService.search(new SearchQuery("*")
				.withFilter(IndexFilter.PUBLIC)
				.withFilter(new FieldTextFilter("EXISTS{}:place_population")));

		verifyResults("wiki");

		searchPage.filterBy(new FieldTextFilter("EXISTS{}:place_elevation"));
		verifyResults("transport");
	}

	private void verifyResults(String index){
		assertThat("Field Text should not have caused an error", searchPage.getText(), not(containsString(Errors.Search.HOD)));
		assertThat(searchPage.getText(), not(containsString(Errors.Search.NO_RESULTS)));
		searchPage.searchResult(1).click();
		DocumentViewer documentViewer = DocumentViewer.make(getDriver());
		for(int i = 0; i < SearchPage.RESULTS_PER_PAGE; i++){
			verifyThat(documentViewer.getIndex(), containsString(index));
			documentViewer.next();
		}
		documentViewer.close();
	}


	@Test
	public void testAuthor(){
		String author = "FIFA.com";
		searchPage = searchService.search(new SearchQuery("blatter").withFilter(new ParametricFilter("Author", author)));

		searchPage.searchResult(1).click();
		DocumentViewer documentViewer = DocumentViewer.make(getDriver());

		for(int i = 0; i < searchPage.RESULTS_PER_PAGE; i++){
			verifyThat(documentViewer.getAuthor(), equalToIgnoringCase(author));
			documentViewer.next();
		}

		documentViewer.close();
	}

}
