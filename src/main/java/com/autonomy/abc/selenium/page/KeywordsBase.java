package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public abstract class KeywordsBase extends AppElement implements AppPage{

	public KeywordsBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public WebElement blacklistLink() {
		return findElement(By.xpath(".//a[text() = 'blacklist']"));
	}

	public WebElement createSynonymsLink() {
		return findElement(By.xpath(".//a[text() = 'create synonyms']"));
	}

	public List<String> youSearchedFor() {
		final List<String> youSearchedFor = new ArrayList<>();
		for (final WebElement word : findElements(By.cssSelector(".search-terms-list span"))) {
			youSearchedFor.add(word.getText());
		}
		return youSearchedFor;
	}

	public List<String> getSynonymGroupSynonyms(final String leadSynonym) {
		loadOrFadeWait();
		final List<WebElement> synonyms = synonymGroup(leadSynonym).findElements(By.cssSelector("li span"));
		final List<String> synonymNames = new ArrayList<>();

		for (final WebElement synonym : synonyms){
			if (!synonym.getText().equals("")) {
				synonymNames.add(synonym.getText());
			}
		}

		return synonymNames;
	}

	public WebElement synonymList(final int index) {
		return findElements(By.cssSelector(".synonyms-list")).get(index);
	}

	public List<String> getBlacklistedTerms() {
		final List<String> blacklistedTerms = new ArrayList<>();

		for (final WebElement blacklistTerm : findElements(By.cssSelector(".blacklisted-word"))) {
			blacklistedTerms.add(blacklistTerm.getText());
		}

		return blacklistedTerms;
	}

	public WebElement synonymGroup(final String synonymGroupLead) {
		return getParent(leadSynonym(synonymGroupLead));
	}

	public WebElement leadSynonym(final String synonym) {
		return findElement(By.xpath(".//ul[contains(@class, 'synonyms-list')]/li[1][@data-keyword='" + synonym + "']"));
	}

	public int countSynonymLists() {
		return findElements(By.cssSelector(".keywords-list-container .synonyms-list")).size();
	}

	public void deleteBlacklistedTerm(final String blacklistedTerm) {
		findElement(By.cssSelector("[data-keyword = '" + blacklistedTerm + "'] .remove-blacklisted-term")).click();
		loadOrFadeWait();
	}

	public void addSynonymToGroup(final String synonym, final String synonymGroupLead) {
		final WebElement synonymGroup = synonymGroup(synonymGroupLead);
		synonymGroup.findElement(By.cssSelector(".fa-plus")).click();
		synonymGroup.findElement(By.cssSelector(".add-synonym-input")).clear();
		synonymGroup.findElement(By.cssSelector(".add-synonym-input")).sendKeys(synonym);
		synonymGroup.findElement(By.cssSelector(".fa-check")).click();
		loadOrFadeWait();
	}

	public void deleteSynonym(final String synonym, final String synonymGroupLead) throws InterruptedException {
		synonymGroup(synonymGroupLead).findElement(By.xpath(".//span[contains(text(), '" + synonym + "')]/i")).click();
		Thread.sleep(3000);
	}
}
