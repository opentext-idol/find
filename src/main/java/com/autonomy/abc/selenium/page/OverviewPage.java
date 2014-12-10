package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OverviewPage extends AppElement implements AppPage {

	public OverviewPage(final SideNavBar sideNavBar, final WebElement $el) {
		super($el, sideNavBar.getDriver());
	}

	@Override
	public void navigateToPage() { getDriver().get("overview"); }

	public int searchTermSearchCount(final String searchTerm) {
		return Integer.parseInt(getWidget(Widgets.TOP_SEARCH_TERMS).findElement(By.cssSelector(ACTIVE_TABLE_SELECTOR)).findElement(By.xpath(".//a[text()='" + searchTerm + "']/../../td[3]")).getText());
	}

	public int searchTermRow(final String searchTerm) {
		return Integer.parseInt(getWidget(Widgets.TOP_SEARCH_TERMS).findElement(By.cssSelector(ACTIVE_TABLE_SELECTOR)).findElement(By.xpath(".//a[text()='" + searchTerm + "']/../../td[1]")).getText());
	}

	public final static String ACTIVE_TABLE_SELECTOR = ":not([style='display: none;']) > table";

	public enum Widgets {
		ZERO_HIT_TERMS("Zero Hit Terms"),
		TOP_SEARCH_TERMS("Top Search Terms"),
		WEEKLY_SEARCH("Weekly Search Count"),
		YESTERDAY_SEARCH("Yesterday's Search Count"),
		TODAY_SEARCH("Today's Search Count");

		private final String tabName;

		Widgets(final String name) {
			tabName = name;
		}

		public String toString() {
			return tabName;
		}
	}

	public WebElement getWidget(final Widgets widgetHeadingText) {
		return findElement(By.xpath(".//h5[text()='" + widgetHeadingText.toString() + "']/../.."));
	}

	public WebElement zeroHitLastWeekButton() {
		return getWidget(Widgets.ZERO_HIT_TERMS).findElement(By.xpath(".//*[@value='week']/.."));
	}

	public WebElement zeroHitLastDayButton() {
		return getWidget(Widgets.ZERO_HIT_TERMS).findElement(By.xpath(".//*[@value='day']/.."));
	}

	public WebElement topSearchTermsLastTimePeriodButton(final String timePeriod) {
		return getWidget(Widgets.TOP_SEARCH_TERMS).findElement(By.xpath(".//*[@value='" + timePeriod + "']/.."));
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<OverviewPage> {

		public Placeholder(final AppBody body, final SideNavBar sideNavBar, final TopNavBar topNavBar) {
			super(body, sideNavBar, topNavBar, "overview", NavBarTabId.OVERVIEW, false);
		}

		@Override
		protected OverviewPage convertToActualType(final WebElement element) {
			return new OverviewPage(navBar, element);
		}

	}
}
