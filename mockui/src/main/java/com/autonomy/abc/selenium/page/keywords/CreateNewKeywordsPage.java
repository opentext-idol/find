package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.language.Language;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateNewKeywordsPage extends AppElement implements AppPage {

	public CreateNewKeywordsPage(final WebDriver driver) {
		super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
	}

	@Override
	public void waitForLoad(){
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("pd-wizard")));
	}

	public WebElement keywordsType(final KeywordType type) {
		return findElement(By.xpath(".//h4[contains(text(), '" + type.getTitle() + "')]/../.."));
	}

	public WebElement keywordsType(final KeywordType type, final WebDriverWait wait) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath(".//h4[contains(text(), '" + type.getTitle() + "')]/../..")));
	}

	@Deprecated // no longer exists
	public WebElement backButton() {
		return new WebDriverWait(getDriver(),4).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()[contains(.,'Back')]]")));
	}

	public enum KeywordType {
		SYNONYM("Synonyms"),
		BLACKLIST("Blacklisted Terms");

		private final String title;

		KeywordType(final String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

	public WebElement cancelWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .cancel-wizard"));
	}

	public enum WizardStep {
		TYPE("type"),
		TRIGGERS("triggers"),
		FINISH("finish-step");

		private final String title;

		WizardStep(final String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

	public WebElement previousWizardButton(){
		return findElement(By.cssSelector(".wizard-controls .previous-step"));
	}

	public WebElement continueWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .next-step"));
	}

	public WebElement synonymAddButton() {
		return findElement(By.cssSelector(".synonyms-input-view [type='submit']"));
	}

	public WebElement blacklistAddButton() {
		return findElement(By.cssSelector("[data-branch='blacklisted']")).findElement(By.xpath(".//i[contains(@class, 'fa-plus')]/.."));
	}

	// use keywordAddInput instead
	public WebElement synonymAddTextBox() {
		return findElement(By.cssSelector(".synonyms-input-view [name='words']"));
	}

	public FormInput keywordAddInput() {
		return new FormInput(findElement(By.cssSelector(".wizard-branch:not(.hidden) input[name='words']")), getDriver());
	}

	// use keywordAddInput instead
	public WebElement blacklistAddTextBox() {
		return findElement(By.cssSelector("[data-branch='blacklisted'] .form-group [name='words']"));
	}

	public WebElement finishWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .finish-step"));
	}

	public WebElement enabledFinishWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .finish-step:not([disabled])"));
	}

	public void addSynonyms(final String synonyms) {
		final WebElement addSynonymsTextBox = synonymAddTextBox();
		addSynonymsTextBox.clear();
		addSynonymsTextBox.sendKeys(synonyms);
		tryClickThenTryParentClick(synonymAddButton());
		loadOrFadeWait();
	}

	public void addBlacklistedTerms(final String blacklistedTerms) {
		final WebElement addBlacklistedTextBox = blacklistAddTextBox();
		addBlacklistedTextBox.clear();
		addBlacklistedTextBox.sendKeys(blacklistedTerms);
		tryClickThenTryParentClick(blacklistAddButton());
	}

	public int countKeywords() {
		loadOrFadeWait();
		return findElements(By.cssSelector(".remove-word")).size();
	}

    public int countKeywords(KeywordFilter keywordType) {
        WebElement keywords;

        if (keywordType == KeywordFilter.BLACKLIST){
            keywords = findElement(By.xpath("//div[@data-branch='blacklisted']"));
        } else {
			keywords = findElement(By.xpath("//div[@data-branch='synonyms']"));
		}

        return keywords.findElements(By.cssSelector(".remove-word")).size();
    }

	// use KeywordService.addSynonymGroup
	@Deprecated
	public void createSynonymGroup(final String synonymGroup, final String language) {
		loadOrFadeWait();

		keywordsType(KeywordType.SYNONYM, new WebDriverWait(getDriver(),15)).click();
		selectLanguage(language);
		loadOrFadeWait();
		continueWizardButton().click();
		loadOrFadeWait();
		addSynonyms(synonymGroup);
		loadOrFadeWait();
		(new WebDriverWait(getDriver(),10)).until(ExpectedConditions.elementToBeClickable(enabledFinishWizardButton())).click();
	}

	// use KeywordService.addBlacklistTerms
	@Deprecated
	public void createBlacklistedTerm(final String blacklistedTerm, final String language) {
		keywordsType(KeywordType.BLACKLIST).click();
        selectLanguage(language);
        continueWizardButton().click();
		loadOrFadeWait();
		addBlacklistedTerm(blacklistedTerm);
		loadOrFadeWait();
		enabledFinishWizardButton().click();
	}

	private void addBlacklistedTerm(final String blacklistedTerm) {
		blacklistAddTextBox().clear();
		blacklistAddTextBox().sendKeys(blacklistedTerm);
		loadOrFadeWait();
		blacklistAddButton().click();
		loadOrFadeWait();
	}

	public void deleteKeyword(final String keyword) {
		findElement(By.xpath(".//span[contains(text(), '" + keyword + "')]/i")).click();
		loadOrFadeWait();
	}

	public List<String> getProspectiveKeywordsList() {
		final List<String> keywordsList = new ArrayList<>();

		for(final WebElement word : findElements(By.xpath(".//i[contains(@class, 'remove-word')]/.."))) {
			if(word.isDisplayed()) {
				keywordsList.add(word.getText());
			}
		}

		return keywordsList;
	}

	public WebElement languagesSelectBox() {
		return findElement(By.cssSelector("[data-step='type'] .dropdown-toggle"));
	}

	public abstract void selectLanguage(final String language);

	public void selectLanguage(final Language language) {
		selectLanguage(language.toString());
	}
}
