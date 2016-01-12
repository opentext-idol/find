package com.autonomy.abc.search;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.not;

public class SearchPageHostedITCase extends HostedTestBase {
	private SearchPage searchPage;
	private SearchService searchService;

	public SearchPageHostedITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
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
	//TODO make this test WAY nicer
	public void testAuthor(){
		searchService.search(new SearchQuery("fruit").withFilter(IndexFilter.PUBLIC));
		assertThat(searchPage, not(containsText(Errors.Search.HOD)));

		String author = "FIFA.COM";

		searchPage.openParametricValuesList();

		int results = searchPage.filterByAuthor(author);

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		Waits.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();

		assertThat(searchPage.getHeadingResultsCount(), is(results));

		searchPage.getSearchResult(1).click();

		for(int i = 0; i < results; i++) {
			assertThat(new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()[contains(.,'Author')]]/..//li"))).getText(), equalToIgnoringCase(author));
			getDriver().findElement(By.className("fa-chevron-circle-right")).click();
		}

		getDriver().findElement(By.className("fa-close")).click();

		Waits.loadOrFadeWait();

		searchPage.filterByAuthor(author); //'Unfilter'

		Waits.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();

		author = "YLEIS";

		results = searchPage.filterByAuthor(author);

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		Waits.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();

		assertThat(searchPage.getHeadingResultsCount(), is(results));

		searchPage.getSearchResult(1).click();

		for(int i = 0; i < results; i++) {
			assertThat(new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()[contains(.,'Author')]]/..//li"))).getText(), is("Yleis"));
			getDriver().findElement(By.className("fa-chevron-circle-right")).click();
		}

		getDriver().findElement(By.className("fa-close")).click();
	}

}
