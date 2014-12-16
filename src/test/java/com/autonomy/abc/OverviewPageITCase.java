package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.page.OverviewPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class OverviewPageITCase extends ABCTestBase{

	public OverviewPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private OverviewPage overviewPage;

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
		assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).getText().contains("Top terms searched for last week"));

		overviewPage.topSearchTermsLastTimePeriodButton("day").click();
		assertThat("last week button is active after last day click", !overviewPage.topSearchTermsLastTimePeriodButton("week").getAttribute("class").contains("active"));
		assertThat("last day button is not active after click", overviewPage.topSearchTermsLastTimePeriodButton("day").getAttribute("class").contains("active"));
		assertThat("last hour button is active after last day click", !overviewPage.topSearchTermsLastTimePeriodButton("hour").getAttribute("class").contains("active"));
		assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).getText().contains("Top terms searched for yesterday"));

		overviewPage.topSearchTermsLastTimePeriodButton("hour").click();
		assertThat("last week button is active after last hour click", !overviewPage.topSearchTermsLastTimePeriodButton("week").getAttribute("class").contains("active"));
		assertThat("last day button is active after last hour click", !overviewPage.topSearchTermsLastTimePeriodButton("day").getAttribute("class").contains("active"));
		assertThat("last hour button is not active after click", overviewPage.topSearchTermsLastTimePeriodButton("hour").getAttribute("class").contains("active"));
		assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).getText().contains("Top terms searched for in the last hour"));
	}

	@Test
	public void testTopSearchTermsLinks() throws UnsupportedEncodingException {
		for (final String timeUnit : Arrays.asList("hour", "day", "week")) {
			overviewPage.topSearchTermsLastTimePeriodButton(timeUnit).click();

			if (!overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).getText().contains("No data")) {
				final List<WebElement> tableRowLinks = overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).findElements(By.cssSelector(OverviewPage.ACTIVE_TABLE_SELECTOR + " .table a"));

				for (final WebElement tableRowLink : tableRowLinks) {
					if (tableRowLink.isDisplayed()) {
						String searchTerm = tableRowLink.getText();
						tableRowLink.click();
						final SearchPage searchPage = body.getSearchPage();
						new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));

						if (getDriver().getCurrentUrl().contains(searchTerm)) {
							assertThat(searchTerm + " URL incorrect", getDriver().getCurrentUrl().contains("search/modified/" + searchTerm));
							searchTerm = URLDecoder.decode(searchTerm, "UTF-8");
							assertThat(searchTerm + " Title incorrect", searchPage.title().contains("Results for " + searchTerm));
						} else {
							searchTerm = URLDecoder.decode(searchTerm, "UTF-8");
							assertThat(searchTerm + " URL incorrect", getDriver().getCurrentUrl().contains("search/modified/" + searchTerm));
							assertThat(searchTerm + " Title incorrect", searchPage.title().contains("Results for " + searchTerm));
						}

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

			if (!overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).getText().contains("No data")) {
				final List<WebElement> tableRowLinks = overviewPage.getWidget(OverviewPage.Widgets.TOP_SEARCH_TERMS).findElements(By.cssSelector(OverviewPage.ACTIVE_TABLE_SELECTOR + " a"));
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

}
