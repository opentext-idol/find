package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class KeywordsPage extends KeywordsBase implements AppPage {

	public KeywordsPage(final SideNavBar sideNavBar, final WebElement $el) {
		super($el, sideNavBar.getDriver());
	}

	@Override
	public void navigateToPage() {
		getDriver().get("promotions");
	}

	public WebElement createNewKeywordsButton() {
		return findElement(By.xpath(".//a[contains(text(), 'Create new keywords')]"));
	}

	public void deleteAllSynonyms() throws InterruptedException {
		final int numberOfSynonymGroups = findElements(By.cssSelector("li:first-child .remove-search-synonym")).size();

		if (numberOfSynonymGroups >= 2) {
			for (int i = 0; i <= numberOfSynonymGroups; i++) {
				if (findElements(By.cssSelector("li:first-child .remove-search-synonym")).size() > 2) {
					findElement(By.cssSelector("li:first-child .remove-search-synonym")).click();
					Thread.sleep(3000);
				} else {
					findElement(By.cssSelector("li:first-child .remove-search-synonym")).click();
					break;
				}
			}
		}
	}

	public void deleteAllBlacklistedTerms() {
		for (final WebElement blacklisted : findElements(By.cssSelector(".remove-blacklisted-term"))) {
			blacklisted.click();
		}
	}

	public void filterView(final String filter) {
		findElement(By.cssSelector(".search-filter .dropdown-toggle")).click();
		findElement(By.cssSelector(".search-filter [data-type='" + filter + "']")).click();
	}

	public int countSynonymGroupsWithLeadSynonym(final String synonym) {
		return findElement(By.cssSelector(".keywords-list")).findElements(By.xpath(".//ul[contains(@class, 'synonyms-list')]/li[1][@data-keyword='" + synonym + "']")).size();
	}

	public WebElement searchFilterTextBox() {
		return findElement(By.cssSelector(".search-filter .form-control"));
	}

	public static class Placeholder extends AbstractMainPagePlaceholder<KeywordsPage> {

		public Placeholder(final AppBody body, final SideNavBar sideNavBar, final TopNavBar topNavBar) {
			super(body, sideNavBar, topNavBar, "keywords", NavBarTabId.KEYWORDS, false);
		}

		@Override
		protected KeywordsPage convertToActualType(final WebElement element) {
			return new KeywordsPage(navBar, element);
		}

	}
}
