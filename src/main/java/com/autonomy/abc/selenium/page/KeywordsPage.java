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
		return findElement(By.xpath(".//*[contains(text(), 'Create new keywords')]"));
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

	public WebElement addSynonymsTextBox() {
		return findElement(By.cssSelector(".keywords-add-synonyms [type='text']"));
	}

	public WebElement finishWizardButton() {
		return findElement(By.cssSelector("[data-step='synonyms'] .finish-step"));
	}

	public void addSynonyms(final String synonyms) {
		addSynonymsTextBox().clear();
		addSynonymsTextBox().sendKeys(synonyms);
		addSynonymsButton().click();
	}

	public int countKeywords() {
		return findElements(By.cssSelector(".remove-keyword")).size();
	}

	public WebElement synonymLink(final String synonym) {
		return findElement(By.xpath(".//ul[contains(@class, 'synonyms-list')]/li[1]/a/span[contains(text(), '" + synonym + "')]"));

	}

	public List<String> getSynonymGroup(final String leadSynonym) {
		final List<WebElement> synonyms = getParent(getParent(getParent(synonymLink(leadSynonym)))).findElements(By.cssSelector("li a"));
		final List<String> synonymNames = new ArrayList<>();

		for (final WebElement synonym : synonyms){
			synonymNames.add(synonym.getText());
		}

		return synonymNames;
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
