package com.autonomy.abc.selenium.page.keywords;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

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

	@Deprecated
	public abstract WebElement leadSynonym(final String synonym);

	public abstract WebElement synonymInGroup(final String synonym);

	public WebElement synonymGroup(final String synonymGroupLead) {
		return getParent(getParent(leadSynonym(synonymGroupLead)));
	}

	public void addSynonymToGroup(final String synonym, final String synonymGroupLead) {
		synonymGroupPlusButton(synonymGroupLead).click();
		synonymGroupTextBox(synonymGroupLead).clear();
		synonymGroupTextBox(synonymGroupLead).sendKeys(synonym);
		synonymGroupTickButton(synonymGroupLead).click();
		waitForRefreshIconToDisappear();
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
		LoggerFactory.getLogger(KeywordsBase.class).info("Deleting '"+synonym+"' from '"+synonymGroupLead+"'");
		getSynonymIcon(synonym, synonymGroupLead).click();
		waitForRefreshIconToDisappear();
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
		waitForRefreshIconToDisappear();
	}

	@Deprecated
	public WebElement getSynonymIcon(final String synonym, final String synonymLead) {
		return getSynonymIcon(synonym);
	}

	public WebElement getSynonymIcon(final String synonym){
		return findElement(By.xpath("//span[text()='"+synonym+"']/../i"));
	}

	public boolean areAnyKeywordsDisabled() {
		return countDisabledKeywords() > 0;
	}

	public int countDisabledKeywords() {
		return findElements(By.cssSelector(".keywords-list-container .disabled")).size();
	}

	public void waitForRefreshIconToDisappear() {
		WebDriverWait wait = new WebDriverWait(getDriver(),60);
		wait.withMessage("Waiting for refresh icons to disappear");
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver webDriver) {
				List<WebElement> refreshIcons = webDriver.findElements(By.className("fa-refresh"));

				int visibleRefreshIcons = 0;

				try {
					for (WebElement icon : refreshIcons) {
						if (icon.isDisplayed()) {
							visibleRefreshIcons++;
						}
					}
				} catch (StaleElementReferenceException e) {
					//NOOP
				}

				return visibleRefreshIcons == 0;
			}
		});
	}

	public int countRefreshIcons() {
		try {
            List<WebElement> refreshIcons = findElements(By.cssSelector(".keywords-list .fa-refresh"));

            int visibleIcons = 0;

            for(WebElement refresh : refreshIcons){
                if (refresh.isDisplayed()){
                    visibleIcons++;
                }
            }

            return visibleIcons;
        } catch (Exception e){
            return 0;
        }
	}
}
