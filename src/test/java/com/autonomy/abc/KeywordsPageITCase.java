package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.page.KeywordsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;


public class KeywordsPageITCase extends ABCTestBase {
	public KeywordsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private KeywordsPage keywordsPage;
	private SearchPage searchPage;

	@Before
	public void setUp() throws MalformedURLException {
		keywordsPage = body.getKeywordsPage();
	}


	@Test
	public void testCreateNewKeywordsButtonAndCancel() {
		assertThat("Create new keywords button is not visible", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		assertThat("Create new keywords button should not be visible", !keywordsPage.createNewKeywordsButton().isDisplayed());
		assertThat("Create Synonyms button should be visible", keywordsPage.keywordsType("SYNONYMS").isDisplayed());
		assertThat("Create Blacklisted button should be visible", keywordsPage.keywordsType("BLACKLISTED").isDisplayed());
		assertThat("Cancel button be visible", keywordsPage.cancelWizardButton("type").isDisplayed());
		assertThat("Continue button should be visible", keywordsPage.continueWizardButton("type").isDisplayed());

		keywordsPage.cancelWizardButton("type").click();
		assertThat("Create new keywords button should be visible", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateSynonymsWizard() {
		keywordsPage.createNewKeywordsButton().click();
		assertThat("Continue button should be disabled until a keywords type is selected", keywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		keywordsPage.keywordsType("SYNONYMS").click();
		assertThat("Synonym type not set active", keywordsPage.keywordsType("SYNONYMS").getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", !keywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		keywordsPage.continueWizardButton("type").click();
		assertThat("Finish button should be disabled until synonyms are added", keywordsPage.finishWizardButton().getAttribute("class").contains("disabled"));

		keywordsPage.addSynonymsTextBox().clear();
		assertThat("Finish button should be disabled until synonyms are added", keywordsPage.finishWizardButton().getAttribute("class").contains("disabled"));
		assertThat("Finish button should be disabled until synonyms are added", keywordsPage.addSynonymsButton().getAttribute("class").contains("disabled"));

		keywordsPage.addSynonymsTextBox().sendKeys("horse");
		assertThat("Finish button should be disabled until synonyms are added", keywordsPage.finishWizardButton().getAttribute("class").contains("disabled"));

		keywordsPage.addSynonymsButton().click();
		assertThat("Finish button should be disabled until more than one synonym is added", keywordsPage.finishWizardButton().getAttribute("class").contains("disabled"));
		assertEquals(1, keywordsPage.countKeywords());

		keywordsPage.addSynonyms("stuff more things");
		assertThat("Finish button should be enabled", !keywordsPage.finishWizardButton().getAttribute("class").contains("disabled"));
		assertEquals(4, keywordsPage.countKeywords());

		keywordsPage.finishWizardButton().click();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		final List<String> searchTerms = searchPage.getSearchTermsList();
		assertThat("Synonym group does not contain 'stuff'", searchTerms.contains("stuff"));
		assertThat("Synonym group does not contain 'horse'", searchTerms.contains("horse"));
		assertThat("Synonym group does not contain 'more'", searchTerms.contains("more"));
		assertThat("Synonym group does not contain 'things'", searchTerms.contains("things"));

		navBar.getTab(NavBarTabId.KEYWORDS).click();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym horse is not displayed", keywordsPage.synonymLink("horse").isDisplayed());
		final List<String> synonymGroup = keywordsPage.getSynonymGroup("horse");
		assertThat("Synonym group does not contain 'stuff'", synonymGroup.contains("stuff"));
		assertThat("Synonym group does not contain 'horse'", synonymGroup.contains("horse"));
		assertThat("Synonym group does not contain 'more'", synonymGroup.contains("more"));
		assertThat("Synonym group does not contain 'things'", synonymGroup.contains("things"));
	}
}