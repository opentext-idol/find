package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.language.LanguageDropdown;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public abstract class CreateNewKeywordsPage extends SOPageBase {

	public CreateNewKeywordsPage(final WebDriver driver) {
		super(waitForLoad(driver), driver);
	}

	@Override
	public void waitForLoad(){
		waitForLoad(getDriver());
	}

	protected static WebElement waitForLoad(WebDriver driver) {
		return new WebDriverWait(driver,30)
				.withMessage("waiting for keywords wizard to load")
				.until(ExpectedConditions.visibilityOf(containerElement(driver)));
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

	public WebElement previousWizardButton(){
		return findElement(By.cssSelector(".wizard-controls .previous-step"));
	}

	public WebElement continueWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .next-step"));
	}

	public WebElement finishWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .finish-step"));
	}

	public WebElement enabledFinishWizardButton() {
		return findElement(By.cssSelector(".wizard-controls .finish-step:not([disabled])"));
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

	public TriggerForm getTriggerForm(){
		return new TriggerForm(findElement(By.cssSelector(".wizard-branch:not(.hidden)")), getDriver());
	}
}
