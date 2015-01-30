package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

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
		loadOrFadeWait();
		filterView("Synonyms");

		for (final String language : getLanguageList()) {
			selectLanguage(language);
			final int numberOfSynonymGroups = findElements(By.cssSelector("li:first-child .remove-search-synonym")).size();

			if (numberOfSynonymGroups >= 2) {
				for (int i = 0; i <= numberOfSynonymGroups; i++) {
					if (findElements(By.cssSelector("li:first-child .remove-search-synonym")).size() > 2) {
						findElement(By.cssSelector("li:first-child .remove-search-synonym")).click();
						Thread.sleep(3000);
					} else {
						if (findElements(By.cssSelector("li:first-child .remove-search-synonym")).size() == 2) {
							findElement(By.cssSelector("li:first-child .remove-search-synonym")).click();
						}
						break;
					}
				}
			}
		}
	}

	private int getNumberOfLanguages() {
		return findElements(By.cssSelector(".scrollable-menu li")).size();
	}

	public void deleteAllBlacklistedTerms() {
		filterView("Blacklist");

		for (final String language : getLanguageList()) {
			selectLanguage(language);
			for (final WebElement blacklisted : findElements(By.cssSelector(".remove-blacklisted-term"))) {
				blacklisted.click();
			}
		}
	}

	public void filterView(final String filter) {
		findElement(By.cssSelector(".keywords-filters .dropdown-toggle")).click();
		findElement(By.xpath(".//a[text()='" + filter + "']")).click();
	}

	public int countSynonymGroupsWithLeadSynonym(final String synonym) {
		return findElement(By.cssSelector(".keywords-list")).findElements(By.xpath(".//ul[contains(@class, 'synonyms-list')]/li[1][@data-keyword='" + synonym + "']")).size();
	}

	public WebElement searchFilterTextBox() {
		return findElement(By.cssSelector(".search-filter .form-control"));
	}

	public void selectLanguage(final String language) {
		if (!getSelectedLanguage().equals(language)) {
			getParent(selectLanguageButton()).click();

			final WebElement element = findElement(By.cssSelector(".keywords-filters")).findElement(By.xpath(".//a[text()='" + language + "']"));
			// IE doesn't like clicking dropdown elements
			final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
			executor.executeScript("arguments[0].click();", element);
			loadOrFadeWait();
		}
	}

	public String getSelectedLanguage() {
		return selectLanguageButton().getText();
	}

	public WebElement selectLanguageButton() {
		return findElement(By.cssSelector(".keywords-filters .current-language-selection"));
	}

	public List<String> getLanguageList() {
		final List<String> languages = new ArrayList<>();

		if (isAttributePresent(getParent(selectLanguageButton()), "disabled")) {
			languages.add(getSelectedLanguage());
			return languages;
		} else {
			selectLanguageButton().click();
			loadOrFadeWait();

			for (final WebElement language : findElements(By.cssSelector(".scrollable-menu a"))) {
				languages.add(language.getText());
			}

			selectLanguageButton().click();
			return languages;
		}
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
