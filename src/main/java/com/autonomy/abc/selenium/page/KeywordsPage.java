package com.autonomy.abc.selenium.page;


import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.util.AbstractMainPagePlaceholder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class KeywordsPage extends AppElement implements AppPage {

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

	public WebElement leadSynonym(final String synonym) {
		return findElement(By.xpath(".//ul[contains(@class, 'synonyms-list')]/li[1][@data-keyword='" + synonym + "']"));
	}

	public List<String> getSynonymGroupSynonyms(final String leadSynonym) {
		final List<WebElement> synonyms = synonymGroup(leadSynonym).findElements(By.cssSelector("li button"));
		final List<String> synonymNames = new ArrayList<>();

		for (final WebElement synonym : synonyms){
			if (!synonym.getText().equals("")) {
				synonymNames.add(synonym.getText());
			}
		}

		return synonymNames;
	}

	public List<String> getBlacklistedTerms() {
		final List<String> blacklistedTerms = new ArrayList<>();

		for (final WebElement blacklistTerm : findElements(By.cssSelector(".blacklisted-word"))) {
			blacklistedTerms.add(blacklistTerm.getText());
		}

		return blacklistedTerms;
	}

	public void deleteSynonym(final String synonym, final String synonymGroupLead) throws InterruptedException {
		synonymGroup(synonymGroupLead).findElement(By.xpath(".//button[contains(text(), '" + synonym + "')]/i")).click();
		Thread.sleep(3000);
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

	public int countSynonymLists() {
		final List<String> synonymLists = new ArrayList<>();
		for (final WebElement synonymList : findElements(By.cssSelector(".keywords-list-container .synonyms-list"))) {
			if (!synonymList.getText().equals("")) {
				synonymLists.add(synonymList.getText());
			}
		}
		return synonymLists.size();
	}

	public WebElement synonymList(final int index) {
		return findElements(By.cssSelector(".synonyms-list")).get(index);
	}

	public int countSynonymGroupsWithLeadSynonym(final String synonym) {
		return findElement(By.cssSelector(".keywords-list")).findElements(By.xpath(".//ul[contains(@class, 'synonyms-list')]/li[1][@data-keyword='" + synonym + "']")).size();
	}

	public void addSynonymToGroup(final String synonym, final String synonymGroupLead) {
		final WebElement synonymGroup = synonymGroup(synonymGroupLead);
		synonymGroup.findElement(By.cssSelector(".fa-plus")).click();
		synonymGroup.findElement(By.cssSelector(".add-synonym-input")).clear();
		synonymGroup.findElement(By.cssSelector(".add-synonym-input")).sendKeys(synonym);
		synonymGroup.findElement(By.cssSelector(".fa-check")).click();
		loadOrFadeWait();
	}

	public WebElement synonymGroup(final String synonymGroupLead) {
		return getParent(leadSynonym(synonymGroupLead));
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
