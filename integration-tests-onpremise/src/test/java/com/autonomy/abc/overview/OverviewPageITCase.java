package com.autonomy.abc.overview;

import com.autonomy.abc.base.SOTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.autonomy.abc.selenium.analytics.IsoOverviewPage;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.iso.OPISOElementFactory;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.*;

public class OverviewPageITCase extends SOTestBase {

	public OverviewPageITCase(final TestConfig config) {
		super(config);
	}

	private IsoOverviewPage overviewPage;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Override
	public OPISOElementFactory getElementFactory() {
		return (OPISOElementFactory) super.getElementFactory();
	}

	@Before
	public void setUp() {
		overviewPage = getApplication().switchTo(IsoOverviewPage.class);
	}

	@Test
	public void testZeroHitTermsGraphToggleButtons() {
		overviewPage.zeroHitLastWeekButton().click();
		assertThat("last week button is not active after click", overviewPage.zeroHitLastWeekButton(), hasClass("active"));
		assertThat("last day button is active after last week click", overviewPage.zeroHitLastDayButton(), not(hasClass("active")));

		overviewPage.zeroHitLastDayButton().click();
		assertThat("last week button is active after last day click", overviewPage.zeroHitLastWeekButton(), not(hasClass("active")));
		assertThat("last day button is not active after click", overviewPage.zeroHitLastDayButton(), hasClass("active"));
	}

    @Test
	public void testTopSearchTermsToggleButtons() {
        List<String> buttons = Arrays.asList("week", "day", "hour");

		overviewPage.topSearchTermsLastTimePeriodButton(buttons.get(0)).click();
		assertToggleButtons(buttons, buttons.get(0), "last week");

        overviewPage.topSearchTermsLastTimePeriodButton(buttons.get(1)).click();
		assertToggleButtons(buttons, buttons.get(1), "yesterday");

		overviewPage.topSearchTermsLastTimePeriodButton(buttons.get(2)).click();
		assertToggleButtons(buttons, buttons.get(2), "in the last hour");
	}

    private void assertToggleButtons(List<String> toggleButtons, String active, String widgetText) {
        for(String button : toggleButtons){
            if(button.equals(active)) {
                assertThat(overviewPage.topSearchTermsLastTimePeriodButton(button), hasClass("active"));
            } else {
                assertThat(overviewPage.topSearchTermsLastTimePeriodButton(button), not(hasClass("active")));
            }
        }

        assertThat("Widget text not changed", overviewPage.getWidget(IsoOverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("Top terms searched for " + widgetText));
    }
	@Test
	public void testTopSearchTermsLinks() {
		for (final String timeUnit : Arrays.asList("hour", "day", "week")) {
			overviewPage.topSearchTermsLastTimePeriodButton(timeUnit).click();

			if (!overviewPage.getWidget(IsoOverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("No data")) {
				final List<WebElement> tableRowLinks = overviewPage.getWidget(IsoOverviewPage.Widget.TOP_SEARCH_TERMS).findElements(By.cssSelector(IsoOverviewPage.ACTIVE_TABLE_SELECTOR + " .table a"));

				for (final WebElement tableRowLink : tableRowLinks) {
					if (tableRowLink.isDisplayed()) {
						final String searchTerm = tableRowLink.getText();
						tableRowLink.click();
						SearchPage searchPage = getElementFactory().getSearchPage();
						assertThat(getWindow(), urlContains("search/modified/" + searchTerm));
						assertThat(searchTerm + " Title incorrect", searchPage.getPageTitle(), containsString("Results for " + searchTerm));

						getApplication().switchTo(IsoOverviewPage.class);
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
			Waits.loadOrFadeWait();

			if (!overviewPage.getWidget(IsoOverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("No data")) {
				final List<WebElement> tableRowLinks = overviewPage.getWidget(IsoOverviewPage.Widget.TOP_SEARCH_TERMS).findElements(By.cssSelector(IsoOverviewPage.ACTIVE_TABLE_SELECTOR + " a"));
				final List<Integer> searchCounts = new ArrayList<>(Collections.nCopies(10, 0));

				for (final WebElement tableRowLink : tableRowLinks) {
					if (!tableRowLink.getText().equals("")) {
						final int rowIndex = tableRowLinks.indexOf(tableRowLink);
						final int searchCount = overviewPage.searchTermSearchCount(tableRowLink.getText());
						searchCounts.add(rowIndex, searchCount);
					}
				}

				for (int i = 0; i < 9; i++) {
					assertThat("Row " + Integer.toString(i + 1) + " is out of place", searchCounts.get(i), greaterThanOrEqualTo(searchCounts.get(i + 1)));
				}
			}
		}
	}

	@Test
	public void testZeroHitTermsLinks() throws UnsupportedEncodingException, InterruptedException {
		getApplication().keywordService().deleteAll(KeywordFilter.ALL);
		getApplication().switchTo(IsoOverviewPage.class);
		String extraSynonym = "apple";

		final List<WebElement> tableLinks = overviewPage.getWidget(IsoOverviewPage.Widget.ZERO_HIT_TERMS).findElements(By.cssSelector(".table a"));

		for (final WebElement tableLink : tableLinks) {
			final String linkText = tableLink.getText();
			tableLink.click();

            final CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

            TriggerForm triggerForm = createNewKeywordsPage.getTriggerForm();

			assertThat("Have not linked to synonyms wizard", createNewKeywordsPage.getText(), containsString("Select synonyms"));
			assertThat(triggerForm.getNumberOfTriggers(), is(1));
			assertThat("incorrect synonym in prospective keywords list", triggerForm.getTriggersAsStrings(), hasItem(linkText));
			assertThat("finish button should be disabled", ElementUtil.isAttributePresent(createNewKeywordsPage.finishWizardButton(), "disabled"));

			extraSynonym += "z";
			triggerForm.addTrigger(extraSynonym);
			assertThat(triggerForm.getNumberOfTriggers(), is(2));
			assertThat("incorrect synonym in prospective keywords list", triggerForm.getTriggersAsStrings(), hasItems(linkText, extraSynonym));
			assertThat("finish button should be enabled", !ElementUtil.isAttributePresent(createNewKeywordsPage.finishWizardButton(), "disabled"));

			createNewKeywordsPage.finishWizardButton().click();

			final SearchPage searchPage = getElementFactory().getSearchPage();

			if (!searchPage.getText().contains("An error occurred executing the search action")) {
				assertThat("page title incorrect", searchPage.getPageTitle(), containsString(linkText));
				assertThat("page title incorrect", searchPage.getPageTitle(), containsString(extraSynonym));
				assertThat(searchPage.getSearchResults(), not(empty()));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor(), hasItems(linkText, extraSynonym));
				assertThat(searchPage.countSynonymLists(), is(2));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(linkText), hasItem(extraSynonym));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(extraSynonym), hasItem(linkText));

				final String searchResultTitle = searchPage.getSearchResult(1).getTitleString();
                getElementFactory().getTopNavBar().search(linkText);
				assertThat("page title incorrect", searchPage.getPageTitle(), containsString(linkText));
				assertThat(searchPage.getSearchResults(), not(empty()));
				assertThat(searchPage.getSearchResult(1).getTitleString(), is(searchResultTitle));
				assertThat(searchPage.countSynonymLists(), is(1));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(linkText), hasItem(extraSynonym));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor(), hasItem(linkText));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor(), not(hasItem(extraSynonym)));
			} else {
				System.out.println(linkText + " returns a search error as part of a synonym group");
			}

			getApplication().switchTo(IsoOverviewPage.class);
		}
	}

    private boolean percentageIsNumber(IsoOverviewPage.Widget widgetElement) {
        try {
            overviewPage.getZeroHitPercentageParseInt(widgetElement);
        } catch(NumberFormatException e) {
            return false;
        }

        return true;
    }

	@Test
	public void testPercentageOfQueriesWithZeroHits() {

		assertThat(overviewPage.getZeroHitQueries(IsoOverviewPage.Widget.TODAY_SEARCH), lessThanOrEqualTo(overviewPage.getTotalSearches(IsoOverviewPage.Widget.TODAY_SEARCH)));
		assertThat(overviewPage.getZeroHitQueries(IsoOverviewPage.Widget.YESTERDAY_SEARCH), lessThanOrEqualTo(overviewPage.getTotalSearches(IsoOverviewPage.Widget.YESTERDAY_SEARCH)));
		assertThat(overviewPage.getZeroHitQueries(IsoOverviewPage.Widget.WEEKLY_SEARCH), lessThanOrEqualTo(overviewPage.getTotalSearches(IsoOverviewPage.Widget.WEEKLY_SEARCH)));

        if(percentageIsNumber(IsoOverviewPage.Widget.TODAY_SEARCH)) {
			assertThat(Math.round((overviewPage.getZeroHitQueries(IsoOverviewPage.Widget.TODAY_SEARCH) * 100f) / overviewPage.getTotalSearches(IsoOverviewPage.Widget.TODAY_SEARCH)),
					is(overviewPage.getZeroHitPercentageParseInt(IsoOverviewPage.Widget.TODAY_SEARCH)));
        } else {
			assertThat(overviewPage.getZeroHitPercentage(IsoOverviewPage.Widget.TODAY_SEARCH), is("N/A"));
        }

        if (percentageIsNumber(IsoOverviewPage.Widget.YESTERDAY_SEARCH)) {
			assertThat(Math.round((overviewPage.getZeroHitQueries(IsoOverviewPage.Widget.YESTERDAY_SEARCH) * 100f) / overviewPage.getTotalSearches(IsoOverviewPage.Widget.YESTERDAY_SEARCH)),
					is(overviewPage.getZeroHitPercentageParseInt(IsoOverviewPage.Widget.YESTERDAY_SEARCH)));
        } else {
			assertThat(overviewPage.getZeroHitPercentage(IsoOverviewPage.Widget.YESTERDAY_SEARCH), is("N/A"));
        }

        if (percentageIsNumber(IsoOverviewPage.Widget.WEEKLY_SEARCH)) {
			assertThat(Math.round((overviewPage.getZeroHitQueries(IsoOverviewPage.Widget.WEEKLY_SEARCH) * 100f) / overviewPage.getTotalSearches(IsoOverviewPage.Widget.WEEKLY_SEARCH)),
					is(overviewPage.getZeroHitPercentageParseInt(IsoOverviewPage.Widget.WEEKLY_SEARCH)));
        } else {
			assertThat(overviewPage.getZeroHitPercentage(IsoOverviewPage.Widget.WEEKLY_SEARCH), is("N/A"));
        }
	}
}
