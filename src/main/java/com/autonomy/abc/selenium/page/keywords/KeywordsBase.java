package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public abstract class KeywordsBase extends AppElement implements AppPage {

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

	public WebElement synonymList(final int index) {
		return findElements(By.cssSelector(".keywords-sub-list")).get(index);
	}

	public List<String> getBlacklistedTerms() {
		final List<String> blacklistedTerms = new ArrayList<>();

		for (final WebElement blacklistTerm : findElements(By.cssSelector(".blacklisted-word .keyword-label-text"))) {
			blacklistedTerms.add(blacklistTerm.getText());
		}

		return blacklistedTerms;
	}

	public abstract WebElement leadSynonym(final String synonym);

	public WebElement synonymGroup(final String synonymGroupLead) {
		return getParent(getParent(leadSynonym(synonymGroupLead)));
	}

	public void addSynonymToGroup(final String synonym, final String synonymGroupLead) {
		synonymGroupPlusButton(synonymGroupLead).click();
		synonymGroupTextBox(synonymGroupLead).clear();
		synonymGroupTextBox(synonymGroupLead).sendKeys(synonym);
		synonymGroupTickButton(synonymGroupLead).click();
		loadOrFadeWait();
	}

	public WebElement synonymGroupPlusButton(final String synonymGroupLead) {
		return synonymGroup(synonymGroupLead).findElement(By.cssSelector(".fa-plus"));
	}

	public WebElement synonymGroupTickButton(final String synonymGroupLead) {
		return synonymGroup(synonymGroupLead).findElement(By.cssSelector(".fa-check"));
	}

	public WebElement synonymGroupTextBox(final String synonymGroupLead) {
		return synonymGroup(synonymGroupLead).findElement(By.cssSelector("[name='new-synonym']"));
	}

	public void deleteSynonym(final String synonym, final String synonymGroupLead) throws InterruptedException {
		getSynonymIcon(synonym, synonymGroupLead).click();
		Thread.sleep(3000);
	}

	public List<String> getSynonymGroupSynonyms(final String leadSynonym) {
		loadOrFadeWait();
		final List<WebElement> synonyms = synonymGroup(leadSynonym).findElements(By.cssSelector("li span span"));
		final List<String> synonymNames = new ArrayList<>();

		for (final WebElement synonym : synonyms){
			if (!synonym.getText().equals("")) {
				synonymNames.add(synonym.getText());
			}
		}

		return synonymNames;
	}

	public void deleteBlacklistedTerm(final String blacklistedTerm) {
		findElement(By.cssSelector("[data-term = '" + blacklistedTerm + "'] .blacklisted-word .remove-keyword")).click();
		loadOrFadeWait();
	}

	public WebElement getSynonymIcon(final String synonym, final String synonymLead) {
		return synonymGroup(synonymLead).findElement(By.xpath(".//span[contains(text(), '" + synonym + "')]/../i"));
	}
}
