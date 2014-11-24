package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.MainTabBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OverviewPage extends AppElement implements AppPage {

	public OverviewPage(final MainTabBar mainTabBar, final WebElement $el) {
		super($el, mainTabBar.getDriver());
	}

	@Override
	public void navigateToPage() { getDriver().get("overview"); }

	public WebElement getWidget(final String widgetHeadingText) {
		return findElement(By.xpath("//h5[text()='" + widgetHeadingText + "']/../.."));
	}

	public WebElement zeroHitLastWeekButton() {
		return getWidget("Zero Hit Queries").findElement(By.xpath(".//*[@value='week']/.."));
	}

	public WebElement zeroHitLastDayButton() {
		return getWidget("Zero Hit Queries").findElement(By.xpath(".//*[@value='day']/.."));
	}

	public WebElement topSearchTermsLastTimePeriodButton(final String timePeriod) {
		return getWidget("Top Search Terms").findElement(By.xpath(".//*[@value='" + timePeriod + "']/.."));
	}

	public void widgetCollapseExpand(final String widgetHeadingText) {
		getWidget(widgetHeadingText).findElement(By.cssSelector(".ibox-collapse")).click();
		loadOrFadeWait();
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<OverviewPage> {

		public Placeholder(final AppBody body, final MainTabBar mainTabBar, final TopNavBar topNavBar) {
			super(body, mainTabBar, topNavBar, "overview", "overview", false);
		}

		@Override
		protected OverviewPage convertToActualType(final WebElement element) {
			return new OverviewPage(tabBar, element);
		}

	}
}
