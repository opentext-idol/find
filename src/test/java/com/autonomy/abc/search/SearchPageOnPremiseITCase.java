package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.thoughtworks.selenium.SeleneseTestBase.assertNotEquals;
import static com.thoughtworks.selenium.SeleneseTestBase.assertTrue;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
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
		searchPage = getElementFactory().getSearchPage();
	}


	@Test
	public void testDatabaseSelection() {
		topNavBar.search("car");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		final List<String> databasesList = searchPage.getAllDatabases();
		final List<WebElement> databaseCheckboxes = searchPage.getDatabaseCheckboxes();
		for (int i = 0; i < databasesList.size(); i++) {
			assertTrue("Database '" + databasesList.get(i) + "' is not selected", databaseCheckboxes.get(i).isSelected());
		}
		assertTrue("'All' databases checkbox is not selected", searchPage.allDatabasesCheckbox().isSelected());

		for (final String database : databasesList) {
			if (!database.equals("wikienglish")) {
				searchPage.deselectDatabase(database);
			}
		}
		assertFalse("'All' databases checkbox is not selected", searchPage.allDatabasesCheckbox().isSelected());
		assertEquals("Only one database should be selected", 1, searchPage.getSelectedDatabases().size());
		assertThat("Correct database not selected", searchPage.getSelectedDatabases().contains("wikienglish"));
		final String wikiEnglishResult = searchPage.getSearchResult(1).getText();

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertTrue("Only results from filtered database should be showing", searchPage.getSearchResultDetails(i).contains("wikienglish"));
			}
			searchPage.javascriptClick(searchPage.forwardPageButton());
			searchPage.loadOrFadeWait();
		}

		searchPage.backToFirstPageButton().click();
		searchPage.selectDatabase("wookiepedia");
		searchPage.deselectDatabase("wikienglish");
		assertEquals("Only one database should be selected", 1, searchPage.getSelectedDatabases().size());
		assertThat("Correct database not selected", searchPage.getSelectedDatabases().contains("wookiepedia"));
		final String wookiepediaResult = searchPage.getSearchResult(1).getText();
		assertNotEquals(wookiepediaResult, wikiEnglishResult);

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertTrue("Only results from filtered database should be showing", searchPage.getSearchResultDetails(i).contains("wookiepedia"));
			}
			searchPage.javascriptClick(searchPage.forwardPageButton());
		}

		searchPage.backToFirstPageButton().click();
		searchPage.selectDatabase("wikienglish");
		assertEquals("Only one database should be selected", 2, searchPage.getSelectedDatabases().size());
		assertThat("Correct databases not showing", searchPage.getSelectedDatabases().containsAll(Arrays.asList("wookiepedia", "wikienglish")));
		assertThat("Search result not from selected databases", searchPage.getSearchResult(1).getText().equals(wookiepediaResult) || searchPage.getSearchResult(1).getText().equals(wikiEnglishResult));

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertTrue("Only results from filtered database should be showing", searchPage.getSearchResultDetails(i).contains("wikienglish") || searchPage.getSearchResultDetails(i).contains("wookiepedia"));
			}
			searchPage.javascriptClick(searchPage.forwardPageButton());
		}
	}

}
