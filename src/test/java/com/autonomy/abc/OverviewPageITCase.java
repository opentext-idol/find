package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.OverviewPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.net.MalformedURLException;

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
}
