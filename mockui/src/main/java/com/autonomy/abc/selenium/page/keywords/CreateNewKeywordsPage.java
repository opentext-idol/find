package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
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
		super(containerElement(driver), driver);
	}

	@Override
	public void waitForLoad(){
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated(By.className("pd-wizard")));
	}

	public WebElement keywordsType(final KeywordType type) {
		return findElement(By.xpath(".//h4[contains(text(), '" + type.getTitle() + "')]/../.."));
	}

	public List<List<String>> getExistingSynonymGroups() {
		List<List<String>> groups = new ArrayList<>();
		for(WebElement group : findElements(By.cssSelector(".keywords-existing-synonyms-list .keywords-sub-list"))){
			List<String> terms = new ArrayList<>();
			for(WebElement term : group.findElements(By.tagName("li"))){
				terms.add(term.getText());
			}

			groups.add(terms);
		}

		return groups;
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
		Waits.loadOrFadeWait();
		ElementUtil.tryClickThenTryParentClick(synonymAddButton());
		Waits.loadOrFadeWait();
	}

	public void addBlacklistedTerms(final String blacklistedTerms) {
		final WebElement addBlacklistedTextBox = blacklistAddTextBox();
		addBlacklistedTextBox.clear();
		addBlacklistedTextBox.sendKeys(blacklistedTerms);
		ElementUtil.tryClickThenTryParentClick(blacklistAddButton());
	}

	public int countKeywords() {
		Waits.loadOrFadeWait();
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

	public void deleteKeyword(final String keyword) {
		findElement(By.xpath(".//span[contains(text(), '" + keyword + "')]/i")).click();
		Waits.loadOrFadeWait();
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

	public void selectLanguage(final Language language) {
		languageDropdown().select(language);
	}

	protected abstract LanguageDropdown languageDropdown();

	private static WebElement containerElement(WebDriver driver) {
		// this ensures that we get the keywords wizard, not any other (promotions) wizard
		WebElement keywordsContainer = driver.findElement(By.className("keywords-container"));
		WebElement parent = ElementUtil.ancestor(keywordsContainer, 1);
		return parent.findElement(By.className("pd-wizard"));
	}
}
