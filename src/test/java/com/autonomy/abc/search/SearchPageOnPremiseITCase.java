package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.thoughtworks.selenium.SeleneseTestBase.assertNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchPageOnPremiseITCase extends ABCTestBase {
	public SearchPageOnPremiseITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = body.getSearchPage();
	}


	@Test
	public void testDatabaseSelection() {
		topNavBar.search("car");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
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

}
