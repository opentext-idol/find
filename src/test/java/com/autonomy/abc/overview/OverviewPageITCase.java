package com.autonomy.abc.overview;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.overview.OverviewPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;

import static com.thoughtworks.selenium.SeleneseTestBase.assertEquals;
import static com.thoughtworks.selenium.SeleneseTestBase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OverviewPageITCase extends ABCTestBase{

	public OverviewPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private OverviewPage overviewPage;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
		overviewPage = body.getOverviewPage();
	}

	@Test
	public void testZeroHitTermsGraphToggleButtons() {
		overviewPage.zeroHitLastWeekButton().click();
		assertThat("last week button is not active after click", overviewPage.zeroHitLastWeekButton().getAttribute("class").contains("active"));
		assertThat("last day button is active after last week click", !overviewPage.zeroHitLastDayButton().getAttribute("class").contains("active"));

		overviewPage.zeroHitLastDayButton().click();
		assertThat("last week button is active after last day click", !overviewPage.zeroHitLastWeekButton().getAttribute("class").contains("active"));
		assertThat("last day button is not active after click", overviewPage.zeroHitLastDayButton().getAttribute("class").contains("active"));
	}

	@Test
	public void testTopSearchTermsToggleButtons() {
		overviewPage.topSearchTermsLastTimePeriodButton("week").click();
		assertThat("last week button is not active after click", overviewPage.topSearchTermsLastTimePeriodButton("week").getAttribute("class").contains("active"));
		assertThat("last day button is active after last week click", !overviewPage.topSearchTermsLastTimePeriodButton("day").getAttribute("class").contains("active"));
		assertThat("last hour button is active after last week click", !overviewPage.topSearchTermsLastTimePeriodButton("hour").getAttribute("class").contains("active"));
		assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("Top terms searched for last week"));

		overviewPage.topSearchTermsLastTimePeriodButton("day").click();
		assertThat("last week button is active after last day click", !overviewPage.topSearchTermsLastTimePeriodButton("week").getAttribute("class").contains("active"));
		assertThat("last day button is not active after click", overviewPage.topSearchTermsLastTimePeriodButton("day").getAttribute("class").contains("active"));
		assertThat("last hour button is active after last day click", !overviewPage.topSearchTermsLastTimePeriodButton("hour").getAttribute("class").contains("active"));
		assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("Top terms searched for yesterday"));

		overviewPage.topSearchTermsLastTimePeriodButton("hour").click();
		assertThat("last week button is active after last hour click", !overviewPage.topSearchTermsLastTimePeriodButton("week").getAttribute("class").contains("active"));
		assertThat("last day button is active after last hour click", !overviewPage.topSearchTermsLastTimePeriodButton("day").getAttribute("class").contains("active"));
		assertThat("last hour button is not active after click", overviewPage.topSearchTermsLastTimePeriodButton("hour").getAttribute("class").contains("active"));
		assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("Top terms searched for in the last hour"));
	}

	@Test
	public void testTopSearchTermsLinks() {
		for (final String timeUnit : Arrays.asList("hour", "day", "week")) {
			overviewPage.topSearchTermsLastTimePeriodButton(timeUnit).click();

			if (!overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("No data")) {
				final List<WebElement> tableRowLinks = overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).findElements(By.cssSelector(OverviewPage.ACTIVE_TABLE_SELECTOR + " .table a"));

				for (final WebElement tableRowLink : tableRowLinks) {
					if (tableRowLink.isDisplayed()) {
						final String searchTerm = tableRowLink.getText();
						tableRowLink.click();
						final SearchPage searchPage = body.getSearchPage();
						new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
						assertThat(searchTerm + " URL incorrect", getDriver().getCurrentUrl().contains("search/modified/" + searchTerm));
						assertThat(searchTerm + " Title incorrect", searchPage.title().contains("Results for " + searchTerm));

						navBar.switchPage(NavBarTabId.OVERVIEW);
					}
				}
			} else {
				System.out.println("No data in table for the last " + timeUnit);
			}
		}
	}

	@Test
	public void testTopSearchTermsOrdering() {
		for (final String timeUnit : Arrays.asList("hour", "day", "week")) {
			overviewPage.topSearchTermsLastTimePeriodButton(timeUnit).click();
			overviewPage.loadOrFadeWait();

			if (!overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("No data")) {
				final List<WebElement> tableRowLinks = overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).findElements(By.cssSelector(OverviewPage.ACTIVE_TABLE_SELECTOR + " a"));
				final List<Integer> searchCounts = new ArrayList<>(Collections.nCopies(10, 0));

				for (final WebElement tableRowLink : tableRowLinks) {
					if (!tableRowLink.getText().equals("")) {
						final int rowIndex = tableRowLinks.indexOf(tableRowLink);
						final int searchCount = overviewPage.searchTermSearchCount(tableRowLink.getText());
						searchCounts.add(rowIndex, searchCount);
					}
				}

				for (int i = 0; i < 9; i++) {
					assertThat("Row " + Integer.toString(i + 1) + " is out of place", searchCounts.get(i) >= searchCounts.get(i + 1));
				}
			}
		}
	}

	@Test
	public void testZeroHitTermsLinks() throws UnsupportedEncodingException, InterruptedException {
		final CreateNewKeywordsPage createNewKeywordsPage = body.getCreateKeywordsPage();
		final KeywordsPage keywordsPage = body.getKeywordsPage();
		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		navBar.switchPage(NavBarTabId.OVERVIEW);
		String extraSynonym = "apple";

		final List<WebElement> tableLinks = overviewPage.getWidget(OverviewPage.Widget.ZERO_HIT_TERMS).findElements(By.cssSelector(".table a"));

		for (final WebElement tableLink : tableLinks) {
			final String linkText = tableLink.getText();
			tableLink.click();

			assertThat("Have not linked to synonyms wizard", createNewKeywordsPage.getText().contains("Select synonyms"));
			assertEquals(1, createNewKeywordsPage.countKeywords());
			assertThat("incorrect synonym in prospective keywords list", createNewKeywordsPage.getProspectiveKeywordsList().contains(linkText));
			assertThat("finish button should be disabled", createNewKeywordsPage.isAttributePresent(createNewKeywordsPage.finishWizardButton(), "disabled"));

			extraSynonym += "z";
			createNewKeywordsPage.addSynonyms(extraSynonym);
			assertEquals(2, createNewKeywordsPage.countKeywords());
			assertThat("incorrect synonym in prospective keywords list", createNewKeywordsPage.getProspectiveKeywordsList().containsAll(Arrays.asList(linkText, extraSynonym)));
			assertThat("finish button should be enabled", !createNewKeywordsPage.isAttributePresent(createNewKeywordsPage.finishWizardButton(), "disabled"));

			createNewKeywordsPage.finishWizardButton().click();

			final SearchPage searchPage = body.getSearchPage();
			new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

			if (!searchPage.getText().contains("An error occurred executing the search action")) {
				assertThat("page title incorrect", searchPage.title().contains(linkText));
				assertThat("page title incorrect", searchPage.title().contains(extraSynonym));
				assertThat("no search results displayed", searchPage.docLogo().isDisplayed());
				assertThat("you searched for section incorrect", searchPage.youSearchedFor().containsAll(Arrays.asList(linkText, extraSynonym)));
				assertEquals(2, searchPage.countSynonymLists());
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(linkText).contains(extraSynonym));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(extraSynonym).contains(linkText));

				final String searchResultTitle = searchPage.getSearchResultTitle(1);
				topNavBar.search(linkText);
				assertThat("page title incorrect", searchPage.title().contains(linkText));
				assertThat("no search results displayed", searchPage.docLogo().isDisplayed());
				assertEquals(searchResultTitle, searchPage.getSearchResultTitle(1));
				assertEquals(1, searchPage.countSynonymLists());
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(linkText).contains(extraSynonym));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor().contains(linkText));
				assertThat("you searched for section incorrect", !searchPage.youSearchedFor().contains(extraSynonym));
			} else {
				System.out.println(linkText + " returns a search error as part of a synonym group");
			}

			navBar.switchPage(NavBarTabId.OVERVIEW);
		}
	}

	@Test
	public void testPercentageOfQueriesWithZeroHits() {
		assertTrue(overviewPage.getZeroHitQueries(OverviewPage.Widget.TODAY_SEARCH) <= overviewPage.getTotalSearches(OverviewPage.Widget.TODAY_SEARCH));
		assertTrue(overviewPage.getZeroHitQueries(OverviewPage.Widget.YESTERDAY_SEARCH) <= overviewPage.getTotalSearches(OverviewPage.Widget.YESTERDAY_SEARCH));
		assertTrue(overviewPage.getZeroHitQueries(OverviewPage.Widget.WEEKLY_SEARCH) <= overviewPage.getTotalSearches(OverviewPage.Widget.WEEKLY_SEARCH));
		assertEquals(Math.round((overviewPage.getZeroHitQueries(OverviewPage.Widget.TODAY_SEARCH) * 100f) / overviewPage.getTotalSearches(OverviewPage.Widget.TODAY_SEARCH)), overviewPage.getZeroHitPercentage(OverviewPage.Widget.TODAY_SEARCH));
		assertEquals(Math.round((overviewPage.getZeroHitQueries(OverviewPage.Widget.YESTERDAY_SEARCH) * 100f) / overviewPage.getTotalSearches(OverviewPage.Widget.YESTERDAY_SEARCH)), overviewPage.getZeroHitPercentage(OverviewPage.Widget.YESTERDAY_SEARCH));
		assertEquals(Math.round((overviewPage.getZeroHitQueries(OverviewPage.Widget.WEEKLY_SEARCH) * 100f) / overviewPage.getTotalSearches(OverviewPage.Widget.WEEKLY_SEARCH)), overviewPage.getZeroHitPercentage(OverviewPage.Widget.WEEKLY_SEARCH));
	}
}
