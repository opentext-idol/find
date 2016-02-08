package com.autonomy.abc.selenium.page.keywords;

import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class KeywordsBase extends AppElement implements AppPage {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public KeywordsBase(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public WebElement synonymList(final int index) {
		return findElements(By.cssSelector(".keywords-sub-list")).get(index);
	}

	public List<String> getBlacklistedTerms() {
		return ElementUtil.getTexts(keywordsContainer().blacklistTerms());
	}

	public void addSynonymToGroup(final String synonym, final SynonymGroup group) {
		group.add(synonym);
	}

	public WebElement synonymGroupPlusButton(final String synonymGroupLead) {
		return synonymGroupContaining(synonymGroupLead).synonymAddButton();
	}

	public WebElement synonymGroupTickButton(final String synonymGroupLead) {
		return synonymGroupContaining(synonymGroupLead).tickButton();
	}

	public WebElement synonymGroupTextBox(final String synonymGroupLead) {
		return synonymGroupContaining(synonymGroupLead).synonymInput().getElement();
	}

	public void deleteSynonym(final String synonym) {
		deleteSynonym(synonym, synonymGroupContaining(synonym));
	}

	public void deleteSynonym(final String synonym, final String synonymGroupLead) {
		deleteSynonym(synonym, synonymGroupContaining(synonymGroupLead));
	}

	public void deleteSynonym(String synonym, SynonymGroup synonymGroup){
		logger.info("Deleting '" + synonym + "'");
		synonymGroup.remove(synonym);
	}

	public List<String> getSynonymGroupSynonyms(final String leadSynonym) {
		return synonymGroupContaining(leadSynonym).getSynonyms();
	}

	public void deleteBlacklistedTerm(final String blacklistedTerm) {
		findElement(By.cssSelector("[data-term = '" + blacklistedTerm + "'] .blacklisted-word .remove-keyword")).click();
		waitForRefreshIconToDisappear();
	}

	public void deleteKeyword(final String keyword) {
		findElement(By.cssSelector("[data-term='" + keyword + "'] .remove-keyword")).click();
		waitForRefreshIconToDisappear();
	}

	public WebElement getSynonymIcon(final String synonym){
		return findElement(By.xpath("//span[text()='"+synonym.toLowerCase()+"']/../i"));
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
            int visibleIcons = 0;

            for(WebElement refresh : findElements(By.cssSelector(".keywords-list .fa-spin"))){
                if (refresh.isDisplayed()){
                    visibleIcons++;
                }
            }

            return visibleIcons;
        } catch (Exception e){
            return 0;
        }
	}

	public SynonymGroup synonymGroupContaining(String synonym) {
		return keywordsContainer().synonymGroupContaining(synonym);
	}

	protected KeywordsContainer keywordsContainer() {
        return new KeywordsContainer(findElement(By.cssSelector(".keywords-container")), getDriver());
    }
}
