package com.autonomy.abc.selenium.page.overview;


import com.autonomy.abc.selenium.analytics.DashboardBase;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OverviewPage extends AppElement implements DashboardBase {

    private OverviewPage(final WebDriver driver) {
        super(driver.findElement(By.cssSelector(".wrapper-content")), driver);
    }

	public int searchTermSearchCount(final String searchTerm) {
		return Integer.parseInt(getWidget(Widget.TOP_SEARCH_TERMS).findElement(By.cssSelector(ACTIVE_TABLE_SELECTOR)).findElement(By.xpath(".//a[text()='" + searchTerm + "']/../../td[3]")).getText());
	}

	public final static String ACTIVE_TABLE_SELECTOR = ":not([style='display: none;']) > table";

	public int getTotalSearches(final Widget widget) {
		return Integer.parseInt(getWidget(widget).findElement(By.xpath(".//*[contains(text(), 'Total searches')]/..")).findElement(By.cssSelector(".query-count")).getText().replace(",", ""));
	}

	public int getZeroHitQueries(final Widget widget) {
		return Integer.parseInt(getWidget(widget).findElement(By.xpath(".//*[contains(text(), 'Zero hit queries')]/..")).findElement(By.cssSelector(".query-count")).getText().replace(",", ""));
	}

    public String getZeroHitPercentage(final Widget widget) {
        return getWidget(widget).findElement(By.xpath(".//*[contains(text(), 'Percentage of queries with zero hits')]/..")).findElement(By.cssSelector(".query-count")).getText().split("\\s+")[0];
    }

	public int getZeroHitPercentageParseInt(final Widget widget) {
		return Integer.parseInt(getZeroHitPercentage(widget));
	}

	public enum Widget {
		ZERO_HIT_TERMS("Zero Hit Terms"),
		TOP_SEARCH_TERMS("Top Search Terms"),
		WEEKLY_SEARCH("Weekly Search Count"),
		YESTERDAY_SEARCH("Yesterday's Search Count"),
		TODAY_SEARCH("Today's Search Count");

		private final String tabName;

		Widget(final String name) {
			tabName = name;
		}

		public String toString() {
			return tabName;
		}
	}

	public WebElement getWidget(final Widget widgetHeadingText) {
		return findElement(By.xpath(".//h3[contains(text(), \"" + widgetHeadingText.toString() + "\")]/../.."));
	}

	public WebElement zeroHitLastWeekButton() {
		return getWidget(Widget.ZERO_HIT_TERMS).findElement(By.xpath(".//*[@value='week']/.."));
	}

	public WebElement zeroHitLastDayButton() {
		return getWidget(Widget.ZERO_HIT_TERMS).findElement(By.xpath(".//*[@value='day']/.."));
	}

	public WebElement topSearchTermsLastTimePeriodButton(final String timePeriod) {
		return getWidget(Widget.TOP_SEARCH_TERMS).findElement(By.xpath(".//*[@value='" + timePeriod + "']/.."));
	}

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    public static void waitForLoad(final WebDriver driver) {
        new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.xpath(".//h3[text()='Zero Hit Terms']")));
    }

	public static class Factory implements ParametrizedFactory<WebDriver, OverviewPage> {
		public OverviewPage create(WebDriver context) {
			OverviewPage.waitForLoad(context);
			return new OverviewPage(context);
		}
	}
}
