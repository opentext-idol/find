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
			final int numberOfSynonymGroups = findElements(By.cssSelector(".keywords-list .keywords-sub-list")).size();

			if (numberOfSynonymGroups >= 2) {
				for (int i = 0; i <= numberOfSynonymGroups; i++) {
					if (findElements(By.cssSelector(".keywords-list .keywords-sub-list")).size() > 2) {
						findElement(By.cssSelector(".keywords-list .keywords-sub-list li:first-child .remove-keyword")).click();
						Thread.sleep(3000);
					} else {
						if (findElements(By.cssSelector(".keywords-list .keywords-sub-list")).size() == 2) {
							findElement(By.cssSelector(".keywords-list .keywords-sub-list li:first-child .remove-keyword")).click();
						}

						loadOrFadeWait();
						break;
					}
				}
			}
		}
	}

	public int countSynonymLists() {
		return findElements(By.cssSelector(".keywords-list .keywords-sub-list .btn-default")).size();
	}

	public WebElement leadSynonym(final String synonym) {
		return findElement(By.xpath(".//div[contains(@class, 'keywords-list')]/ul/li/ul[contains(@class, 'keywords-sub-list')]/li[1][@data-term='" + synonym + "']"));
	}

	private int getNumberOfLanguages() {
		return findElements(By.cssSelector(".scrollable-menu li")).size();
	}

	public void deleteAllBlacklistedTerms() {
		filterView("Blacklist");

		for (final String language : getLanguageList()) {
			loadOrFadeWait();
			selectLanguage(language);
			for (final WebElement blacklisted : findElements(By.cssSelector(".blacklisted-word .remove-keyword"))) {
				blacklisted.click();
				loadOrFadeWait();
			}
		}
	}

	public void filterView(final String filter) {
		findElement(By.cssSelector(".keywords-filters .dropdown-toggle")).click();
		loadOrFadeWait();
		findElement(By.xpath(".//a[text()='" + filter + "']")).click();
	}

	public int countSynonymGroupsWithLeadSynonym(final String synonym) {
		return findElement(By.cssSelector(".keywords-list")).findElements(By.xpath(".//ul[contains(@class, 'keywords-sub-list')]/li[1][@data-term='" + synonym + "']")).size();
	}

	public WebElement searchFilterTextBox() {
		return findElement(By.cssSelector(".search-filter .form-control"));
	}

	public void selectLanguage(final String language) {
		if (!getSelectedLanguage().equals(language)) {
			loadOrFadeWait();
			getParent(selectLanguageButton()).click();
			loadOrFadeWait();
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

			for (final WebElement language : findElements(By.cssSelector(".keywords-filters .scrollable-menu a"))) {
				languages.add(language.getText());
			}

			selectLanguageButton().click();
			return languages;
		}
	}

	public List<String> getLeadSynonymsList() {
		final List<String> leadSynonyms = new ArrayList<>();
		for (final WebElement synonymGroup : findElements(By.cssSelector(".keywords-list > ul > li"))) {
			leadSynonyms.add(synonymGroup.findElement(By.cssSelector("li:first-child span span")).getText());
		}
		return leadSynonyms;
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
