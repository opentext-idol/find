package com.autonomy.abc.search;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.ABCAssert;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;

import static com.thoughtworks.selenium.SeleneseTestBase.assertNotEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SearchPageHostedITCase extends HostedTestBase {
	public SearchPageHostedITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.HOSTED);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = getElementFactory().getSearchPage();
	}


	@Test
	public void testIndexSelection() {
		//TODO: Configure test Indexes for Hosted. Below is Database test from on Prem. Follow its structure.
		assert false : "This test needs rewritten to handle indexes";
		topNavBar.search("car");
		searchPage.selectLanguage("English");
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		assertThat("All databases not showing", searchPage.getSelectedDatabases(), hasItem("All"));

		searchPage.selectDatabase("WikiEnglish");
		assertThat("Database not showing", searchPage.getSelectedDatabases(), hasItem("WikiEnglish"));
		final String wikiEnglishResult = searchPage.getSearchResult(1).getText();
		searchPage.deselectDatabase("WikiEnglish");

		searchPage.selectDatabase("Wookiepedia");
		assertThat("Database not showing", searchPage.getSelectedDatabases(), hasItem("Wookiepedia"));
		final String wookiepediaResult = searchPage.getSearchResult(1).getText();
		assertNotEquals(wookiepediaResult, wikiEnglishResult);

		searchPage.selectDatabase("WikiEnglish");
		assertThat("Databases not showing", searchPage.getSelectedDatabases(),hasItems("Wookiepedia", "WikiEnglish"));
		assertThat("Result not from selected databases", searchPage.getSearchResult(1).getText(),anyOf(is(wookiepediaResult),is(wikiEnglishResult)));
	}

	@Test
	public void testParametricSearch() {
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		topNavBar.search("*");

	}


	@Test
	public void testAuthor(){
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//label[text()[contains(.,'news_eng')]]"))).click();

		searchPage.findElement(By.xpath("//label[text()[contains(.,'Public')]]/../i")).click();

		LoggerFactory.getLogger(SearchPageHostedITCase.class).info("Searching for: 'fruit'");
		topNavBar.search("fruit");
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Assert.assertNotEquals(searchPage.getText(), contains("Haven OnDemand returned an error while executing the search action"));

		String author = "RUGBYBWORLDCUP.COM";

		searchPage.openParametricValuesList();

		int results = searchPage.filterByAuthor(author);

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		searchPage.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.loadOrFadeWait();

		ABCAssert.assertThat(searchPage.searchTitle().findElement(By.xpath(".//..//span")).getText(), is("(" + results + ")"));

		searchPage.getSearchResult(1).click();

		for(int i = 0; i < results; i++) {
			ABCAssert.assertThat(new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()[contains(.,'Author')]]/..//li"))).getText(), is(author.toLowerCase()));
			getDriver().findElement(By.className("fa-chevron-circle-right")).click();
		}

		getDriver().findElement(By.className("fa-close")).click();

		searchPage.loadOrFadeWait();

		searchPage.filterByAuthor(author); //'Unfilter'

		searchPage.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.loadOrFadeWait();

		author = "YLEIS";

		results = searchPage.filterByAuthor(author);

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		searchPage.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.loadOrFadeWait();

		ABCAssert.assertThat(searchPage.searchTitle().findElement(By.xpath(".//..//span")).getText(), is("(" + results + ")"));

		searchPage.getSearchResult(1).click();

		for(int i = 0; i < results; i++) {
			ABCAssert.assertThat(new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()[contains(.,'Author')]]/..//li"))).getText(), is("Yleis"));
			getDriver().findElement(By.className("fa-chevron-circle-right")).click();
		}

		getDriver().findElement(By.className("fa-close")).click();
	}

}
