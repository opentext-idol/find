package com.autonomy.abc.overview;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.OPElementFactory;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.overview.OverviewPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.PageUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.openqa.selenium.lift.Matchers.displayed;

public class OverviewPageITCase extends ABCTestBase {

	public OverviewPageITCase(final TestConfig config) {
		super(config);
	}

	private OverviewPage overviewPage;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.ON_PREM);
		return parameters(applicationTypes);
	}

	@Override
	public OPElementFactory getElementFactory() {
		return (OPElementFactory) super.getElementFactory();
	}

	@Before
	public void setUp() {
		getElementFactory().getSideNavBar().switchPage(NavBarTabId.OVERVIEW);
		overviewPage = getElementFactory().getOverviewPage();
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

        assertThat("Widget text not changed", overviewPage.getWidget(OverviewPage.Widget.TOP_SEARCH_TERMS).getText().contains("Top terms searched for " + widgetText));
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
						final SearchPage searchPage = getElementFactory().getSearchPage();
						new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
						assertThat(searchTerm + " URL incorrect", getDriver().getCurrentUrl(), containsString("search/modified/" + searchTerm));
						assertThat(searchTerm + " Title incorrect", PageUtil.getPageTitle(getDriver()), containsString("Results for " + searchTerm));

						getElementFactory().getSideNavBar().switchPage(NavBarTabId.OVERVIEW);
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
					assertThat("Row " + Integer.toString(i + 1) + " is out of place", searchCounts.get(i), greaterThanOrEqualTo(searchCounts.get(i + 1)));
				}
			}
		}
	}

	@Test
	public void testZeroHitTermsLinks() throws UnsupportedEncodingException, InterruptedException {
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        final KeywordsPage keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.deleteKeywords();
		getElementFactory().getSideNavBar().switchPage(NavBarTabId.OVERVIEW);
		String extraSynonym = "apple";

		final List<WebElement> tableLinks = overviewPage.getWidget(OverviewPage.Widget.ZERO_HIT_TERMS).findElements(By.cssSelector(".table a"));

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
			new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

			if (!searchPage.getText().contains("An error occurred executing the search action")) {
				assertThat("page title incorrect", PageUtil.getPageTitle(getDriver()), containsString(linkText));
				assertThat("page title incorrect", PageUtil.getPageTitle(getDriver()), containsString(extraSynonym));
				assertThat("no search results displayed", searchPage.docLogo(1), is(displayed()));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor(), hasItems(linkText, extraSynonym));
				assertThat(searchPage.countSynonymLists(), is(2));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(linkText), hasItem(extraSynonym));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(extraSynonym), hasItem(linkText));

				final String searchResultTitle = searchPage.getSearchResult(1).getTitleString();
                getElementFactory().getTopNavBar().search(linkText);
				assertThat("page title incorrect", PageUtil.getPageTitle(getDriver()), containsString(linkText));
				assertThat("no search results displayed", searchPage.docLogo(1), is(displayed()));
				assertThat(searchPage.getSearchResult(1).getTitleString(), is(searchResultTitle));
				assertThat(searchPage.countSynonymLists(), is(1));
				assertThat("Synonym groups displayed incorrectly", searchPage.getSynonymGroupSynonyms(linkText), hasItem(extraSynonym));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor(), hasItem(linkText));
				assertThat("you searched for section incorrect", searchPage.youSearchedFor(), not(hasItem(extraSynonym)));
			} else {
				System.out.println(linkText + " returns a search error as part of a synonym group");
			}

			getElementFactory().getSideNavBar().switchPage(NavBarTabId.OVERVIEW);
		}
	}

    private boolean percentageIsNumber(OverviewPage.Widget widgetElement) {
        try {
            overviewPage.getZeroHitPercentageParseInt(widgetElement);
        } catch(NumberFormatException e) {
            return false;
        }

        return true;
    }

	@Test
	public void testPercentageOfQueriesWithZeroHits() {

		assertThat(overviewPage.getZeroHitQueries(OverviewPage.Widget.TODAY_SEARCH), lessThanOrEqualTo(overviewPage.getTotalSearches(OverviewPage.Widget.TODAY_SEARCH)));
		assertThat(overviewPage.getZeroHitQueries(OverviewPage.Widget.YESTERDAY_SEARCH), lessThanOrEqualTo(overviewPage.getTotalSearches(OverviewPage.Widget.YESTERDAY_SEARCH)));
		assertThat(overviewPage.getZeroHitQueries(OverviewPage.Widget.WEEKLY_SEARCH), lessThanOrEqualTo(overviewPage.getTotalSearches(OverviewPage.Widget.WEEKLY_SEARCH)));

        if(percentageIsNumber(OverviewPage.Widget.TODAY_SEARCH)) {
			assertThat(Math.round((overviewPage.getZeroHitQueries(OverviewPage.Widget.TODAY_SEARCH) * 100f) / overviewPage.getTotalSearches(OverviewPage.Widget.TODAY_SEARCH)),
					is(overviewPage.getZeroHitPercentageParseInt(OverviewPage.Widget.TODAY_SEARCH)));
        } else {
			assertThat(overviewPage.getZeroHitPercentage(OverviewPage.Widget.TODAY_SEARCH), is("N/A"));
        }

        if (percentageIsNumber(OverviewPage.Widget.YESTERDAY_SEARCH)) {
			assertThat(Math.round((overviewPage.getZeroHitQueries(OverviewPage.Widget.YESTERDAY_SEARCH) * 100f) / overviewPage.getTotalSearches(OverviewPage.Widget.YESTERDAY_SEARCH)),
					is(overviewPage.getZeroHitPercentageParseInt(OverviewPage.Widget.YESTERDAY_SEARCH)));
        } else {
			assertThat(overviewPage.getZeroHitPercentage(OverviewPage.Widget.YESTERDAY_SEARCH), is("N/A"));
        }

        if (percentageIsNumber(OverviewPage.Widget.WEEKLY_SEARCH)) {
			assertThat(Math.round((overviewPage.getZeroHitQueries(OverviewPage.Widget.WEEKLY_SEARCH) * 100f) / overviewPage.getTotalSearches(OverviewPage.Widget.WEEKLY_SEARCH)),
					is(overviewPage.getZeroHitPercentageParseInt(OverviewPage.Widget.WEEKLY_SEARCH)));
        } else {
			assertThat(overviewPage.getZeroHitPercentage(OverviewPage.Widget.WEEKLY_SEARCH), is("N/A"));
        }
	}
}
