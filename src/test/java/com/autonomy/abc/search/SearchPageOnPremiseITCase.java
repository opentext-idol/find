package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SearchPageOnPremiseITCase extends ABCTestBase {
	public SearchPageOnPremiseITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
    private SearchActionFactory searchActionFactory;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
		searchPage = searchActionFactory.makeSearch("example").apply();
	}


	@Test
	public void testDatabaseSelection() {
        searchActionFactory.makeSearch("car").applyFilter(new LanguageFilter("English")).apply();
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		final List<String> databasesList = searchPage.getAllDatabases();
		final List<WebElement> databaseCheckboxes = searchPage.getDatabaseCheckboxes();
		for (int i = 0; i < databasesList.size(); i++) {
			assertThat("Database '" + databasesList.get(i) + "' is not selected", databaseCheckboxes.get(i).isSelected());
		}
		assertThat("'All' databases checkbox is not selected", searchPage.allDatabasesCheckbox().isSelected());

		for (final String database : databasesList) {
			if (!database.equals("wikienglish")) {
				searchPage.deselectDatabase(database);
			}
		}
		assertThat("'All' databases checkbox is not selected", searchPage.allDatabasesCheckbox().isSelected(), is(false));
		assertThat("Only one database should be selected", searchPage.getSelectedDatabases(), hasSize(1));
		assertThat("Correct database not selected", searchPage.getSelectedDatabases(), hasItem("wikienglish"));
		final String wikiEnglishResult = searchPage.getSearchResult(1).getText();

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertThat("Only results from filtered database should be showing", searchPage.getSearchResultDetails(i), containsString("wikienglish"));
			}
			searchPage.javascriptClick(searchPage.forwardPageButton());
			searchPage.loadOrFadeWait();
		}

		searchPage.backToFirstPageButton().click();
		searchPage.selectDatabase("wookiepedia");
		searchPage.deselectDatabase("wikienglish");
		assertThat("Only one database should be selected", searchPage.getSelectedDatabases(), hasSize(1));
		assertThat("Correct database not selected", searchPage.getSelectedDatabases(), hasItem("wookiepedia"));
		final String wookiepediaResult = searchPage.getSearchResult(1).getText();
		assertThat(wookiepediaResult, not(wikiEnglishResult));

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertThat("Only results from filtered database should be showing", searchPage.getSearchResultDetails(i), containsString("wookiepedia"));
			}
			searchPage.javascriptClick(searchPage.forwardPageButton());
		}

		searchPage.backToFirstPageButton().click();
		searchPage.selectDatabase("wikienglish");
		assertThat("Only one database should be selected", searchPage.getSelectedDatabases(), hasSize(2));
		assertThat("Correct databases not showing", searchPage.getSelectedDatabases(), hasItems("wookiepedia", "wikienglish"));
		assertThat("Search result not from selected databases", searchPage.getSearchResult(1).getText(), isOneOf(wookiepediaResult, wikiEnglishResult));

		for (int j = 1; j <= 2; j++) {
			for (int i = 1; i <= 6; i++) {
				assertThat("Only results from filtered database should be showing", searchPage.getSearchResultDetails(i), anyOf(containsString("wikienglish"), containsString("wookiepedia")));
			}
			searchPage.javascriptClick(searchPage.forwardPageButton());
		}
	}

}
