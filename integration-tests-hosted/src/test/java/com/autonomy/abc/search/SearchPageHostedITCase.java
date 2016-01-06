package com.autonomy.abc.search;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Checkbox;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalToIgnoringCase;

public class SearchPageHostedITCase extends HostedTestBase {
	public SearchPageHostedITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private SearchPage searchPage;
	private TopNavBar topNavBar;

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws IOException {
		final Collection<ApplicationType> applicationTypes = Collections.singletonList(ApplicationType.HOSTED);
		return parameters(applicationTypes);
	}

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		topNavBar.search("example");
		searchPage = getElementFactory().getSearchPage();
	}


	@Test
	public void testIndexSelection() {
		topNavBar.search("car");
		searchPage.selectLanguage(Language.ENGLISH);
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		//TODO add a matcher
		assertThat("All databases not showing", searchPage.allIndexesCheckbox().isChecked(), is(true));

		for(Checkbox checkbox : searchPage.indexList()){
			assertThat(checkbox.isChecked(), is(true));
		}

		searchPage.allIndexesCheckbox().toggle();

		searchPage.selectIndex("news_eng");
		assertThat("Database not showing", searchPage.indexCheckbox("news_eng").isChecked(), is(true));
		final String wikiEnglishResult = searchPage.getSearchResult(1).getText();
		searchPage.deselectIndex("news_eng");

		searchPage.selectIndex("news_ger");
		assertThat("Database not showing", searchPage.indexCheckbox("news_ger").isChecked(), is(true));
		final String wookiepediaResult = searchPage.getSearchResult(1).getText();
		assertThat(wookiepediaResult, not(wikiEnglishResult));

		searchPage.selectIndex("wiki_chi");
		assertThat(searchPage.indexCheckbox("news_ger").isChecked(), is(true));
		assertThat(searchPage.indexCheckbox("wiki_chi").isChecked(),is(true));
		assertThat("Result not from selected databases", searchPage.getSearchResult(1).getText(), anyOf(is(wookiepediaResult), is(wikiEnglishResult)));
	}

	@Test
	public void testParametricSearch() {
		searchPage.selectAllIndexesOrDatabases(getConfig().getType().getName());
		topNavBar.search("*");
	}


	@Test
	//TODO make this test WAY nicer
	public void testAuthor(){
		searchPage.findElement(By.xpath("//label[text()[contains(.,'Public')]]/../i")).click();

		topNavBar.search("fruit");
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Assert.assertNotEquals(searchPage.getText(), contains("Haven OnDemand returned an error while executing the search action"));

		String author = "FIFA.COM";

		searchPage.openParametricValuesList();

		int results = searchPage.filterByAuthor(author);

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		Waits.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();

		assertThat(searchPage.getHeadingResultsCount(), is(results));

		searchPage.getSearchResult(1).click();

		for(int i = 0; i < results; i++) {
			assertThat(new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()[contains(.,'Author')]]/..//li"))).getText(), equalToIgnoringCase(author));
			getDriver().findElement(By.className("fa-chevron-circle-right")).click();
		}

		getDriver().findElement(By.className("fa-close")).click();

		Waits.loadOrFadeWait();

		searchPage.filterByAuthor(author); //'Unfilter'

		Waits.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();

		author = "YLEIS";

		results = searchPage.filterByAuthor(author);

		((JavascriptExecutor) getDriver()).executeScript("scroll(0,-400);");

		Waits.loadOrFadeWait();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();

		assertThat(searchPage.getHeadingResultsCount(), is(results));

		searchPage.getSearchResult(1).click();

		for(int i = 0; i < results; i++) {
			assertThat(new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()[contains(.,'Author')]]/..//li"))).getText(), is("Yleis"));
			getDriver().findElement(By.className("fa-chevron-circle-right")).click();
		}

		getDriver().findElement(By.className("fa-close")).click();
	}

}
