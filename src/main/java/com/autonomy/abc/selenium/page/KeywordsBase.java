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

	public void deleteBlacklistedTerm(final String blacklistedTerm) {
		findElement(By.cssSelector("[data-keyword = '" + blacklistedTerm + "'] .remove-blacklisted-term")).click();
		loadOrFadeWait();
	}

}
