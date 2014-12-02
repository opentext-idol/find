package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CreateNewKeywordsPage extends AppElement implements AppPage{

	public CreateNewKeywordsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("keywords/create");
	}

	public WebElement keywordsType(final String type) {
		return findElement(By.cssSelector("[data-keywords-type='" + type + "']"));
	}

	public WebElement cancelWizardButton(final String dataType) {
		return findElement(By.cssSelector("[data-step='" + dataType + "'] .cancel-wizard"));
	}

	public WebElement continueWizardButton(final String dataType) {
		return findElement(By.cssSelector("[data-step='" + dataType + "'] .next-step"));
	}

	public WebElement addSynonymsButton() {
		return findElement(By.cssSelector(".keywords-add-synonyms [type='submit']"));
	}

	public WebElement addBlacklistTermsButton() {
		return findElement(By.cssSelector(".keywords-add-blacklist [type='submit']"));
	}

	public WebElement addSynonymsTextBox() {
		return findElement(By.cssSelector(".keywords-add-synonyms [type='text']"));
	}

	public WebElement addBlacklistedTextBox() {
		return findElement(By.cssSelector(".keywords-add-blacklist input"));
	}

	public WebElement finishSynonymWizardButton() {
		return findElement(By.cssSelector("[data-step='synonyms'] .finish-step"));
	}

	public WebElement finishBlacklistWizardButton() {
		return findElement(By.cssSelector("[data-step='blacklisted'] .finish-step"));
	}

	public void addSynonyms(final String synonyms) {
		final WebElement addSynonymsTextBox = addSynonymsTextBox();
		addSynonymsTextBox.clear();
		addSynonymsTextBox.sendKeys(synonyms);
		addSynonymsButton().click();
	}

	public void addBlacklistedTerms(final String blacklistedTerms) {
		final WebElement addBlacklistedTextBox = addBlacklistedTextBox();
		addBlacklistedTextBox.clear();
		addBlacklistedTextBox.sendKeys(blacklistedTerms);
		addBlacklistTermsButton().click();
	}

	public int countKeywords() {
		return findElements(By.cssSelector(".remove-keyword")).size();
	}

	public void createSynonymGroup(final String synonymGroup) {
		keywordsType("SYNONYMS").click();
		continueWizardButton("type").click();
		addSynonyms(synonymGroup);
		finishSynonymWizardButton().click();
	}

	public void createBlacklistedTerm(final String blacklistedTerm) {
		keywordsType("BLACKLISTED").click();
		continueWizardButton("type").click();
		addBlacklistedTerm(blacklistedTerm);
		finishBlacklistWizardButton().click();
	}

	private void addBlacklistedTerm(final String blacklistedTerm) {
		final WebElement blackListTextBox = findElement(By.cssSelector(".keywords-add-blacklist input"));
		blackListTextBox.clear();
		blackListTextBox.sendKeys(blacklistedTerm);
		addBlacklistTermsButton().click();
	}

	public void deleteKeyword(final String keyword) {
		findElement(By.xpath(".//span[contains(text(), '" + keyword + "')]/i")).click();
		loadOrFadeWait();
	}

	public static class Placeholder {

		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public CreateNewKeywordsPage $createNewKeywordsPage(final WebElement element) {
			return new CreateNewKeywordsPage(topNavBar, element);
		}
	}
}
