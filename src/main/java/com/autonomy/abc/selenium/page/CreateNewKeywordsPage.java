package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class CreateNewKeywordsPage extends AppElement implements AppPage{

	public CreateNewKeywordsPage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("keywords/create");
	}

	// TODO: Enum me
	// takes 'SYNONYMS' or 'BLACKLISTED'
	public WebElement keywordsType(final String type) {
		return findElement(By.cssSelector("[data-option='" + type + "']"));
	}

	// TODO: Enum me
	// takes 'type', 'language' or 'finish-step'
	public WebElement cancelWizardButton(final String dataType) {
		return findElement(By.cssSelector("[data-step='" + dataType + "'] .cancel-wizard"));
	}

	public WebElement continueWizardButton(final String dataType) {
		return findElement(By.cssSelector("[data-step='" + dataType + "'] .next-step"));
	}

	public WebElement addSynonymsButton() {
		return findElement(By.cssSelector(".synonyms-input-view [type='submit']"));
	}

	public WebElement addBlacklistTermsButton() {
		return findElement(By.cssSelector("[data-step='blacklisted']")).findElement(By.xpath(".//i[contains(@class, 'fa-plus')]/.."));
	}

	public WebElement addSynonymsTextBox() {
		return findElement(By.cssSelector(".synonyms-input-view [name='words']"));
	}

	public WebElement addBlacklistedTextBox() {
		return findElement(By.cssSelector("[data-step='blacklisted'] .form-group [name='words']"));
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
		tryClickThenTryParentClick(addSynonymsButton());
	}

	public void addBlacklistedTerms(final String blacklistedTerms) {
		final WebElement addBlacklistedTextBox = addBlacklistedTextBox();
		addBlacklistedTextBox.clear();
		addBlacklistedTextBox.sendKeys(blacklistedTerms);
		tryClickThenTryParentClick(addBlacklistTermsButton());
	}

	public int countKeywords() {
		loadOrFadeWait();
		return findElements(By.cssSelector(".remove-word")).size();
	}

	public void createSynonymGroup(final String synonymGroup, final String language) {
		keywordsType("SYNONYMS").click();
		continueWizardButton("type").click();
		loadOrFadeWait();
		selectLanguage(language);
		continueWizardButton("language").click();
		loadOrFadeWait();
		addSynonyms(synonymGroup);
		loadOrFadeWait();
		finishSynonymWizardButton().click();
		loadOrFadeWait();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".promotions-bucket-button")));
	}

	public void createBlacklistedTerm(final String blacklistedTerm, final String language) {
		keywordsType("BLACKLISTED").click();
		continueWizardButton("type").click();
		loadOrFadeWait();
		selectLanguage(language);
		continueWizardButton("language").click();
		loadOrFadeWait();
		addBlacklistedTerm(blacklistedTerm);
		loadOrFadeWait();
		finishBlacklistWizardButton().click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".keywords-filters .dropdown-toggle")));
	}

	private void addBlacklistedTerm(final String blacklistedTerm) {
		addBlacklistedTextBox().clear();
		addBlacklistedTextBox().sendKeys(blacklistedTerm);
		loadOrFadeWait();
		addBlacklistTermsButton().click();
		loadOrFadeWait();
	}

	public void deleteKeyword(final String keyword) {
		findElement(By.xpath(".//span[contains(text(), '" + keyword + "')]/i")).click();
		loadOrFadeWait();
	}

	public List<String> getProspectiveKeywordsList() {
		final List<String> keywordsList = new ArrayList<>();

		for (final WebElement word : findElements(By.xpath(".//i[contains(@class, 'remove-word')]/.."))) {
			keywordsList.add(word.getText());
		}

		return keywordsList;
	}

	public WebElement languagesSelectBox() {
		return findElement(By.cssSelector("[data-step='language'] .dropdown-toggle"));
	}

	public void selectLanguage(final String language) {
		languagesSelectBox().click();
		loadOrFadeWait();
		final WebElement element = findElement(By.cssSelector("[data-step='language'] .dropdown-menu")).findElement(By.xpath(".//a[contains(text(), '" + language + "')]"));
		// IE doesn't want to click the dropdown elements
		final JavascriptExecutor executor = (JavascriptExecutor)getDriver();
		executor.executeScript("arguments[0].click();", element);
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
