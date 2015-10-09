package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.page.HSOAppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.util.AppElement.getParent;
import static com.thoughtworks.selenium.SeleneseTestBase.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertEquals;

public class KeywordsPageAndWizardITCase extends ABCTestBase {
	public KeywordsPageAndWizardITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private KeywordsPage keywordsPage;
	private CreateNewKeywordsPage createKeywordsPage;
	private SearchPage searchPage;
	private NotificationsDropDown notifications;

	@Before
	public void setUp() throws MalformedURLException {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Initial thread.sleep failed");
		}
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.waitForRefreshIconToDisappear();
		keywordsPage.deleteKeywords();
    }

	private class WaitForNotification implements ExpectedCondition{
		private final String notificationText;

		public WaitForNotification(String notificationText){
			this.notificationText = notificationText;
		}


		@Override
		public Object apply(Object o) {
			if(notificationText.contains("Error")){
				fail("Failed to create keyword");
			}

			return notifications.notificationNumber(1).getText().contains(notificationText);
		}
	}

	@Test
	public void testCreateNewKeywordsButtonAndCancel() {
		assertThat("Create new keywords button is not visible", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(),containsString("keywords/create"));
		assertThat("Create new keywords button should not be visible", !keywordsPage.createNewKeywordsButton().isDisplayed());
		assertThat("Create Synonyms button should be visible", createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).isDisplayed());
		assertThat("Create Blacklisted button should be visible", createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).isDisplayed());
		assertThat("Cancel button be visible", createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).isDisplayed());
		assertThat("Continue button should be visible", createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).isDisplayed());

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		assertThat("Create new keywords button should be visible", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateSynonymsWizard() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("Continue button should be disabled until a keywords type is selected", keywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE), "disabled"));

		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
		assertThat("Synonym type not set active", createKeywordsPage.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM)).getAttribute("class"), containsString("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).getAttribute("class"), not(containsString("disabled")));
		assertThat("languages select should be visible", createKeywordsPage.languagesSelectBox().isDisplayed());

		if(getConfig().getType() == ApplicationType.ON_PREM) {
			createKeywordsPage.selectLanguage("French");
			assertEquals("French", createKeywordsPage.languagesSelectBox().getText());
		} else {
			LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class).warn("Cannot select language for synonyms yet");
		}

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

//		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
//		createKeywordsPage.loadOrFadeWait();

		createKeywordsPage.addSynonymsTextBox().clear();
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertThat("Add synonyms button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.addSynonymsButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("horse");
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsButton().click();
		assertThat("Finish button should be disabled until more than one synonym is added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("stuff pony things");
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertEquals(4, createKeywordsPage.countKeywords());

		createKeywordsPage.enabledFinishWizardButton().click();
		createKeywordsPage.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		searchPage.selectLanguage("French");

		searchPage.waitForSearchLoadIndicatorToDisappear();

		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			searchPage.selectNewsEngIndex();
			searchPage.waitForSearchLoadIndicatorToDisappear();
		}

		searchPage.waitForDocLogo();
		final List<String> searchTerms = searchPage.getSearchTermsList();
		assertThat(searchTerms, hasItems("stuff", "horse", "pony", "things"));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);

		keywordsPage.selectLanguage("French");
		assertThat("synonym horse is not displayed", keywordsPage.leadSynonym("horse").isDisplayed());

		final List<String> synonymGroup = keywordsPage.getSynonymGroupSynonyms("horse");
		assertThat(synonymGroup,hasItems("stuff", "horse", "pony", "things"));
	}

	@Test
	public void testWizardCancelButtonsWorksAfterClickingTheNavBarToggleButton() {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

		body.getTopNavBar().sideBarToggle();
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		body.getTopNavBar().sideBarToggle();
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		body.getTopNavBar().sideBarToggle();
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateBlacklistedWizard() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("Continue button should be disabled until a keywords type is selected", createKeywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE), "disabled"));

		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		assertThat("Blacklisted type not set active", createKeywordsPage.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST)).getAttribute("class"), containsString("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).getAttribute("class"), not(containsString("disabled")));

		assertThat("Wizard did not navigate to languages page", createKeywordsPage.languagesSelectBox().isDisplayed());

		if(getConfig().getType() == ApplicationType.ON_PREM) {
			createKeywordsPage.selectLanguage("Swahili");
			assertEquals("Swahili", createKeywordsPage.languagesSelectBox().getText());

			createKeywordsPage.selectLanguage("English");
			assertEquals("English", createKeywordsPage.languagesSelectBox().getText());
		} else {
			(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for blacklists yet");
		}

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();

		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertThat("Wizard did not navigate to blacklist page", createKeywordsPage.addBlacklistedTextBox().isDisplayed());

		createKeywordsPage.addBlacklistedTextBox().clear();
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("danger");
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTextBox().sendKeys("warning beware scary");
		createKeywordsPage.addBlacklistTermsButton().click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
		assertEquals(4, createKeywordsPage.countKeywords());

		createKeywordsPage.enabledFinishWizardButton().click();

		new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		final List<String> blacklistTerms = keywordsPage.getBlacklistedTerms();
		createKeywordsPage.loadOrFadeWait();
		assertThat(blacklistTerms, hasItems("danger", "warning", "beware", "scary"));
		assertEquals("too many blacklist terms", 4, blacklistTerms.size());
	}

	@Test
	public void testKeywordsFilter() throws InterruptedException {
		final WebDriverWait wait = new WebDriverWait(getDriver(), 5);
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("dog hound canine", "English");

		searchPage = getElementFactory().getSearchPage();
		wait.until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText(), containsString("dog"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText(), containsString("hound"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText(), containsString("canine"));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		wait.until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		keywordsPage.selectLanguage("English");
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog"), hasItems("hound", "canine"));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound"), hasItems("dog", "canine"));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine"), hasItems("dog", "hound"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("illegal", "English");

		new WebDriverWait(getDriver(),20).until(ExpectedConditions.visibilityOf(keywordsPage));

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		new WebDriverWait(getDriver(),45).until(new WaitForNotification("illegal"));

		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms(), hasItem("illegal"));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertThat("Blacklist terms are still visible", keywordsPage.getBlacklistedTerms().size() == 0);
		assertThat("A synonym list on row 2 is not visible", keywordsPage.synonymList(1).isDisplayed(), is(Boolean.FALSE));
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog"), hasItems("hound", "canine"));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound"), hasItems("dog", "canine"));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine"), hasItems("dog", "hound"));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);
		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms(), hasItem("illegal"));
		assertEquals("There should not be a a synonym list on row 2", 0, keywordsPage.countSynonymLists());

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		assertThat("A synonym list should be visible on row 2", keywordsPage.synonymList(1).isDisplayed());
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog"), hasItems("hound", "canine"));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound"), hasItems("dog", "canine"));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine"),hasItems("dog", "hound"));
		assertThat("Blacklist term illegal is not visible", keywordsPage.getBlacklistedTerms(), hasItem("illegal"));
	}

	@Test
	public void testDeleteKeywords() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("frog toad amphibian tadpole", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("frog"), hasItems("frog", "toad", "amphibian", "tadpole"));
		assertEquals("Wrong number of synonym lists displayed", 1, keywordsPage.countSynonymLists());
		assertEquals("Wrong number of synonyms in group frog", 4, keywordsPage.getSynonymGroupSynonyms("frog").size());

		keywordsPage.deleteSynonym("amphibian", "toad");
		assertEquals("Wrong number of synonym lists displayed", 1, keywordsPage.countSynonymLists());
		assertEquals("Wrong number of synonyms in group toad", 3, keywordsPage.getSynonymGroupSynonyms("toad").size());
		assertThat("the synonym amphibian should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("tadpole"), not(hasItems("amphibian")));
		assertThat("the synonym amphibian should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("toad"), not(hasItems("amphibian")));
		assertThat("the synonym amphibian should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("frog"), not(hasItems("amphibian")));

		keywordsPage.deleteSynonym("frog", "frog");
		assertEquals("Wrong number of synonym lists displayed", 1, keywordsPage.countSynonymLists());
		assertEquals("Wrong number of synonyms in group toad", 2, keywordsPage.getSynonymGroupSynonyms("toad").size());
		assertThat("the synonym frog should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("toad"), not(hasItems("frog")));
		assertThat("the synonym frog should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("tadpole"), not(hasItems("frog")));

		keywordsPage.deleteSynonym("tadpole", "toad");
		assertEquals("Wrong number of synonym lists displayed", 0, keywordsPage.countSynonymLists());
	}


	//The keyword 'wine' exists in two different synonym groups. Tests that deleting this keyword does not effect the other synonym group
	@Test
	public void testDeleteSynonymsFromOverlappingSynonymGroups() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("wine merlot shiraz bordeaux", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("wine red scarlet burgundy", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("red"), hasItems("red", "scarlet", "wine", "burgundy"));
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(8, keywordsPage.countKeywords());
		assertEquals(4, keywordsPage.getSynonymGroupSynonyms("burgundy").size());
		assertEquals(4, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(2, keywordsPage.countSynonymGroupsWithSynonym("wine"));

		keywordsPage.deleteSynonym("bordeaux", "shiraz");
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(7, keywordsPage.countKeywords());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(4, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(2, keywordsPage.countSynonymGroupsWithSynonym("wine"));

		keywordsPage.deleteSynonym("burgundy", "red");
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(6, keywordsPage.countKeywords());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(2, keywordsPage.countSynonymGroupsWithSynonym("wine"));

		keywordsPage.deleteSynonym("wine", keywordsPage.getSynonymGroup("red"));
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(5, keywordsPage.countKeywords());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(1, keywordsPage.countSynonymGroupsWithSynonym("wine"));

		keywordsPage.deleteSynonym("shiraz", "wine");
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(4, keywordsPage.countKeywords());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(1, keywordsPage.countSynonymGroupsWithSynonym("wine"));

		keywordsPage.deleteSynonym("scarlet", "red");
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(2, keywordsPage.countKeywords());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(1, keywordsPage.countSynonymGroupsWithSynonym("wine"));

		keywordsPage.deleteSynonym("wine", "merlot");
		assertEquals(0, keywordsPage.countSynonymLists());
	}

	//Duplicate blacklisted terms are not allowed to be created within the same language
	@Test
	public void testCreateDuplicateBlacklist() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("fish", "English");
		new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("Blacklist fish not visible", keywordsPage.getBlacklistedTerms(), hasItem("fish"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("fish");
		createKeywordsPage.addBlacklistTermsButton().click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Duplicate blacklist warning message not present", createKeywordsPage.getText(), containsString("The word \"fish\" is already blacklisted"));
		assertThat("Duplicate blacklist term should not be added", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().clear();
		createKeywordsPage.addBlacklistedTextBox().sendKeys("chips");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Duplicate blacklist warning message has not disappeared", createKeywordsPage.getText(), not(containsString("The word \"fish\" is already blacklisted")));
		assertThat("New blacklist term should be added", createKeywordsPage.countKeywords() == 1);
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.deleteKeyword("chips");
		assertThat("There should be no blacklist terms", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		assertThat("Cancel button redirects to wrong page", getDriver().getCurrentUrl(), endsWith("keywords"));
		assertEquals("Wrong number of blacklisted terms", 1, keywordsPage.getBlacklistedTerms().size());
	}

	//Whitespace of any form should not be added as a blacklisted term
	@Test
	public void testWhitespaceBlacklistTermsWizard() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addBlacklistedTextBox().sendKeys(" ");
		createKeywordsPage.tryClickThenTryParentClick(createKeywordsPage.addBlacklistTermsButton());
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addBlacklistedTextBox().clear();
		createKeywordsPage.addBlacklistedTextBox().click();
		createKeywordsPage.addBlacklistedTextBox().sendKeys(Keys.RETURN);
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addBlacklistedTextBox().sendKeys("\t");
		createKeywordsPage.tryClickThenTryParentClick(createKeywordsPage.addBlacklistTermsButton());
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);
	}

	//Whitespace of any form should not be added as a synonym keyword
	@Test
	public void testWhitespaceSynonymsWizard() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addSynonyms(" ");
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addSynonymsTextBox().clear();
		createKeywordsPage.addSynonymsTextBox().click();
		createKeywordsPage.addSynonymsTextBox().sendKeys(Keys.RETURN);
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addSynonyms("\t");
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addSynonyms("test");
		createKeywordsPage.addSynonyms(" ");
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 1);

		createKeywordsPage.addSynonymsTextBox().clear();
		createKeywordsPage.addSynonymsTextBox().click();
		createKeywordsPage.addSynonymsTextBox().sendKeys(Keys.RETURN);
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 1);

		createKeywordsPage.addSynonyms("\t");
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 1);
	}

	//Odd number of quotes or quotes with blank text should not be able to be added as a synonym keyword
	@Test
	public void testQuotesInSynonymsWizard() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();

		createKeywordsPage.addSynonyms("\"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\"\"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\" \"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("test");
		createKeywordsPage.addSynonyms("\"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\"\"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\" \"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("terms \"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Correct error message not showing", createKeywordsPage.getText(), containsString("Terms have an odd number of quotes, suggesting an unclosed phrase"));

		createKeywordsPage.addSynonyms("\"closed phrase\"");
		assertEquals(2, createKeywordsPage.countKeywords());
		assertThat("Phrase not created", createKeywordsPage.getProspectiveKeywordsList(), hasItem("closed phrase"));
		assertThat("Quotes unescaped", createKeywordsPage.getProspectiveKeywordsList(), not(hasItem("/")));
	}


	//Odd number of quotes or quotes with blank text should not be able to be added as a blacklisted term
	@Test
	public void testQuotesInBlacklistWizard() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

		//createKeywordsPage.selectLanguage("English");
		(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for blacklists yet");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();

		createKeywordsPage.addBlacklistedTerms("\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTerms("\"\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTerms("\" \"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTerms("\"d");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("d\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("\"d\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("s\"d\"d");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("test");
		createKeywordsPage.addBlacklistedTerms("\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTerms("\"\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTerms("\" \"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTerms("\"d");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("d\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("\"d\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("s\"d\"d");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText(),containsString("Terms may not contain quotation marks"));
	}

	@Ignore
	// This takes too long for a nightly test but is a useful test that need run periodically as the application has failed in the past with a large number of synonym groups.
	// Failure can present itself on other pages other than the KeywordsPage
	@Test
	public void testAddLotsOfSynonymGroups() throws IOException, InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		final List<String> groupsOfFiveSynonyms = keywordsPage.loadTextFileLineByLineIntoList("C://dev//res//100.txt");

		for (final String synonymGroup : groupsOfFiveSynonyms) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.createSynonymGroup(synonymGroup, "English");

			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			assertThat("Wrong number of synonym lists", keywordsPage.countSynonymLists() == groupsOfFiveSynonyms.indexOf(synonymGroup) + 1);
		}
	}

	//Whitespace, Odd number of quotes or quotes with blank text, boolean operators or proximity operators should not be able to added as keywords. This test checks they can't be added to existing synonyms on the Keywords Page
	@Test
	public void testAddingWhitespaceQuotesBooleansProximityOperatorsOnKeywordsPage() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("one two three", "English");
		searchPage = (SearchPage) new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

		//keywordsPage.selectLanguage("English");
		(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for blacklists yet");

		keywordsPage.addSynonymToGroup("four", "one");
		keywordsPage.waitForRefreshIconToDisappear();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two"), hasItem("four"));
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(4, keywordsPage.countKeywords());

		keywordsPage.synonymGroupPlusButton("three").click();
		keywordsPage.synonymGroupTextBox("three").clear();
		keywordsPage.synonymGroupTickButton("three").click();
		assertThat("add synonym box should still be displayed", keywordsPage.synonymGroupTextBox("three").isDisplayed());

		keywordsPage.searchFilterTextBox().click();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two"), hasItem("four"));
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(4, keywordsPage.countKeywords());

		notifications = body.getTopNavBar().getNotifications();

		for (final String badSynonym : Arrays.asList(" ", "\t", "\"")) {
			addSynonymKeywordsPage("three",badSynonym);
			assertThat("add synonym box should still be displayed. Offending term is " + badSynonym, keywordsPage.synonymGroupTextBox("three").isDisplayed());

			new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver webDriver) {
					return keywordsPage.synonymGroupTextBox("three").isEnabled();
				}
			});

			keywordsPage.loadOrFadeWait();
			keywordsPage.searchFilterTextBox().click();
			assertThat("there should be four synonyms in a group. Offending term is " + badSynonym, keywordsPage.getSynonymGroupSynonyms("one").size() == 4);
			assertEquals(1, keywordsPage.countSynonymLists());
			assertEquals(4, keywordsPage.countKeywords());
		}

		int synonymGroupSize = 4;
		for(final String operatorSynonym : Arrays.asList("NOT", "NEAR", "DNEAR", "XNEAR", "YNEAR", "NEAR123", "SENTENCE2", "PARAGRAPH3", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN")){
			addSynonymKeywordsPage("three",operatorSynonym);
			keywordsPage.waitForRefreshIconToDisappear();
			assertThat(keywordsPage.getSynonymGroupSynonyms("three"), hasItem(operatorSynonym.toLowerCase()));

			assertEquals(++synonymGroupSize, keywordsPage.getSynonymGroupSynonyms("three").size());
			assertEquals(1,keywordsPage.countSynonymLists());
		}
	}

	private void addSynonymKeywordsPage(String leadSynonym,String addSynonym){
		keywordsPage.synonymGroupPlusButton(leadSynonym).click();
		keywordsPage.synonymGroupTextBox(leadSynonym).clear();
		keywordsPage.synonymGroupTextBox(leadSynonym).sendKeys(addSynonym);
		keywordsPage.synonymGroupTickButton(leadSynonym).click();
	}

	//Phrases should be able to be added as synonyms from the keywords page
	@Test
	public void testPhrasesCanBeAddedAsSynonymsOnKeywordsPage() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("one two three", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		keywordsPage.synonymGroupPlusButton("three").click();
		keywordsPage.synonymGroupTextBox("three").clear();
		keywordsPage.synonymGroupTextBox("three").sendKeys("four and five");
		keywordsPage.synonymGroupTickButton("three").click();

		//This is to make sure 'four and five' have finished adding before checking whether the box is still displayed
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated
				(By.xpath("//div[contains(concat(' ', normalize-space(@class), ' '), 'gritter-without-image')]//p[text()[contains(.,'four and five')]]")));

		assertFalse("New Synonym Box is displayed", keywordsPage.synonymGroupTextBox("three").isDisplayed());
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(4, keywordsPage.countKeywords());
		assertThat(keywordsPage.getSynonymGroupSynonyms("three"), hasItem("four and five"));
	}

	//Notification should drop down on creation of new keywords
	//CSA-1447
	@Test
	public void testNotificationForCreatedBlacklistedTermAndSynonymGroup() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("orange", "English");
		body.getTopNavBar().waitForGritterToClear();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		notifications = body.getTopNavBar().getNotifications();

		body.getTopNavBar().notificationsDropdown();
		assertThat(notifications.notificationNumber(1).getText(), containsString("Added \"orange\" to the blacklist"));

		WebDriverWait wait = new WebDriverWait(getDriver(),15);

		body.getTopNavBar().waitForGritterToClear();
		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl(), containsString("keyword"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("piano keyboard pianoforte", "English");
		body.getTopNavBar().waitForGritterToClear();

		body.getTopNavBar().notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText(), containsString("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText(), containsString("Added \"orange\" to the blacklist"));

		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("keyboard", "piano");
		body.getTopNavBar().waitForGritterToClear();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		body.getTopNavBar().notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText(), containsString("Updated a synonym group containing: piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText(), containsString("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(3).getText(), containsString("Added \"orange\" to the blacklist"));

		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl(), containsString("keyword"));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);
		keywordsPage.selectLanguage("English");
		keywordsPage.deleteBlacklistedTerm("orange");
		body.getTopNavBar().waitForGritterToClear();
		body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);

		body.getTopNavBar().notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText(), containsString("Removed \"orange\" from the blacklist"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText(), containsString("Updated a synonym group containing: piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(3).getText(), containsString("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(4).getText(), containsString("Added \"orange\" to the blacklist"));

		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl(), containsString("keyword"));
	}

	// This only tests the notifications dropdown and not the gritters
	@Test
	public void testHTMLEscapedInNotifications() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("<h1>Hi</h1>", "English");
//		body.waitForGritterToClear();

		new WebDriverWait(getDriver(),60).until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		body.getTopNavBar().waitForGritterToClear();

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		assertThat("Notification text incorrect, HTML not escaped", notifications.notificationNumber(1).getText(),
				not(containsString("Added \"hi\" to the blacklist")));
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText(),
				containsString("Added \"<h1>hi</h1>\" to the blacklist"));

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.elementToBeClickable(notifications.notificationNumber(1))).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl(),containsString("keyword"));
	}

	//CSA-1521
	//Blacklisted terms can be created on the searchpage. This link has often broken
	@Test
	public void testCreateBlacklistedTermFromSearchPage() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		body.getTopNavBar().search("noir");
		searchPage = getElementFactory().getSearchPage();
		searchPage.selectLanguage("French");
		searchPage.waitForSearchLoadIndicatorToDisappear();

		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			searchPage.selectNewsEngIndex();
			searchPage.waitForSearchLoadIndicatorToDisappear();
		}

		assertThat("No results for search noir", searchPage.waitForDocLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.blacklistLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to blacklist wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("link not directing to blacklist wizard", createKeywordsPage.getText(), containsString("Select terms to blacklist"));
		assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.BLACKLIST));
		assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("noir");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.BLACKLIST));
		assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

		createKeywordsPage.enabledFinishWizardButton().click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("Blacklisted term not added", keywordsPage.getBlacklistedTerms().contains("noir"));
	}

	//There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current search term. Often breaks.
	@Test
	public void testCreateSynonymGroupFromSearchPage() throws InterruptedException {
		body.getTopNavBar().search("rouge");
		searchPage = getElementFactory().getSearchPage();
		searchPage.waitForSearchLoadIndicatorToDisappear();

		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			searchPage.selectNewsEngIndex();
			searchPage.waitForSearchLoadIndicatorToDisappear();
		}

		searchPage.selectLanguage("French");

		assertThat("No results for search rouge", searchPage.waitForDocLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl(),containsString("keywords/create"));
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("link not directing to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));
		assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList(), hasItem("rouge"));
		assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("rouge");
		createKeywordsPage.addSynonymsButton().click();
		assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList(), hasItem("rouge"));
		assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().clear();
		createKeywordsPage.addSynonymsTextBox().sendKeys("red");
		createKeywordsPage.addSynonymsButton().click();
		assertEquals(2, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList(),hasItem("rouge"));
		assertThat("keywords list does not include term 'red'", createKeywordsPage.getProspectiveKeywordsList(), hasItem("red"));
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.enabledFinishWizardButton().click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(keywordsPage.selectLanguageButton()));
		//assertEquals("Blacklist has been created in the wrong language", "French", keywordsPage.getSelectedLanguage());

		keywordsPage.selectLanguage("French");

		assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("rouge"),hasItem("red"));
		assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("red"), hasItem("rouge"));
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(2, keywordsPage.countKeywords());
	}

	//There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current multi term search. Often breaks.
	@Test
	public void testCreateSynonymGroupFromMultiTermSearchOnSearchPage() throws InterruptedException {
		body.getTopNavBar().search("lodge dodge podge");
		searchPage = getElementFactory().getSearchPage();
		searchPage.waitForSearchLoadIndicatorToDisappear();

		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			searchPage.selectNewsEngIndex();
			searchPage.waitForSearchLoadIndicatorToDisappear();
		}

		searchPage.selectLanguage("English");

		assertThat("No results for search", searchPage.waitForDocLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		assertThat("link not directing to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));
		assertEquals(3, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
		assertThat("Wrong prospective blacklisted terms added", createKeywordsPage.getProspectiveKeywordsList(), hasItems("lodge", "dodge", "podge"));
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.enabledFinishWizardButton(), "disabled"));

		createKeywordsPage.enabledFinishWizardButton().click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

		keywordsPage.selectLanguage("English");

		assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("lodge"), hasItems("lodge", "dodge", "podge"));
		assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("podge"), hasItems("lodge", "dodge", "podge"));
		assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("dodge"),hasItems("lodge", "dodge", "podge"));

		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.countKeywords());
	}

	@Test
	public void testKeywordsSearchFilter() throws InterruptedException {
		WebDriverWait wait = new WebDriverWait(getDriver(),15);

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("grizzly brownBear bigBear", "English");
		//All keywords should be changed by the application to lowercase in all instances
		final List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		searchPage = getElementFactory().getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title(), containsString(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
			assertThat(synonym + " synonym group not complete in'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
			assertEquals("Wrong number of synonym lists displayed", 1, searchPage.countSynonymLists());
		}

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("honeyBee bumbleBee buzzyBee", "English");
		final List<String> synonymListBees = Arrays.asList("honeybee", "bumblebee", "buzzybee");
		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		searchPage = getElementFactory().getSearchPage();

		for (final String synonym : synonymListBees) {
			assertThat(synonym + " not included in title", searchPage.title(),containsString(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBees));
			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(3, createKeywordsPage.countKeywords());
		}

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("English");
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertEquals(2, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("zz");
		assertEquals(2, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("buzz");
		keywordsPage.loadOrFadeWait();
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.countKeywords());

		for (final String synonym : synonymListBees) {
			assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBees));
			assertThat("synonym included in synonym group: " + synonym + " that should not be there", keywordsPage.getSynonymGroupSynonyms(synonym),not(containsItems(synonymListBears)));
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("a");
		keywordsPage.searchFilterTextBox().sendKeys(Keys.BACK_SPACE);
		keywordsPage.loadOrFadeWait();
		assertEquals(2, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("Bear");
		keywordsPage.loadOrFadeWait();
		assertEquals(1, keywordsPage.countSynonymLists());      //Fails because of capital letter
		assertEquals(3, keywordsPage.countKeywords());

		for (final String synonym : synonymListBears) {
			assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
			assertThat("synonym included in synonym group: " + synonym + " that should not be there", keywordsPage.getSynonymGroupSynonyms(synonym),not(containsItems(synonymListBees)));
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("a");
		keywordsPage.searchFilterTextBox().sendKeys(Keys.BACK_SPACE);
		keywordsPage.loadOrFadeWait();
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(6, keywordsPage.countKeywords());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}
	}

	private Matcher<Iterable<String>> containsItems(List<String> list) {
		return hasItems(list.toArray(new String[list.size()]));
	}

	@Test
	public void testSearchPageKeywords() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
		createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListBears, ' '), "English");
		searchPage = getElementFactory().getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title(),containsString(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
			assertThat(synonym + " synonym group not complete in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(3, searchPage.countKeywords());
		}

		searchPage.addSynonymToGroup("kodiak", "grizzly");
		searchPage.loadOrFadeWait();
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
			assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),hasItem("kodiak"));
			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(4, searchPage.countKeywords());
		}

		searchPage.deleteSynonym("bigbear");
		searchPage.loadOrFadeWait();
		synonymListBears = Arrays.asList("grizzly", "brownbear");
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
			assertThat("bigbear not deleted from group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),not(hasItem("bigbear")));
			assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),hasItem("kodiak"));
			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(3, searchPage.countKeywords());
		}

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("English");
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.countKeywords());

		synonymListBears = Arrays.asList("grizzly", "brownbear", "kodiak");
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " group incomplete", keywordsPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
			assertEquals(1, keywordsPage.countSynonymLists());
			assertEquals(3, keywordsPage.countKeywords());
			assertThat("bigbear not deleted from group " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym), not(hasItem("bigbear")));
		}
	}


	@Test
	//CCUK-2703
	public void testNoBlacklistLinkForBlacklistedSearch() throws InterruptedException {
		body.getTopNavBar().search("wizard");
		searchPage = getElementFactory().getSearchPage();

		searchPage.selectLanguage("Arabic");

		searchPage.blacklistLink().click();
		try {
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.enabledFinishWizardButton().click();
		} catch (final NoSuchElementException e) {
			fail("blacklist link on search page has not navigated to the wizard");
		}

		keywordsPage.selectLanguageButton();	//Wait for select Language button

		if(getConfig().getType().equals(ApplicationType.ON_PREM)){
			assertEquals("Blacklist has been created in the wrong language", "Arabic", keywordsPage.getSelectedLanguage());
		}

		keywordsPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);

		keywordsPage.selectLanguage("Arabic");

		assertThat("Blacklisted term not created", keywordsPage.getBlacklistedTerms(), hasItem("wizard"));

		body.getTopNavBar().search("wizard");
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		searchPage.selectLanguage("Arabic");

		assertThat("'You searched for:' section incorrect", searchPage.youSearchedFor(), hasItem("wizard"));
		assertThat("Keywords incorrect", searchPage.getBlacklistedTerms(), hasItem("wizard"));
		assertThat("link to blacklist or create synonyms should not be present", searchPage.getText(),
				not(containsString("You can create synonyms or blacklist these search terms")));

		searchPage.selectLanguage("English");

		assertThat("Term should not be blacklisted in English", searchPage.getText(),not(containsString("Any query terms were either blacklisted or stop words")));
	}

	@Test
	public void testSynonymGroupMembersSearchWholeGroup() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		final List<String> synonymListCars = Arrays.asList("car", "auto", "motor");
		createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListCars, ' '), "English");

		searchPage = getElementFactory().getSearchPage();

		for (final String synonym : synonymListCars) {
			body.getTopNavBar().search(synonym);

			searchPage.selectLanguage("Swahili", getConfig().getType().getName());

			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(3, createKeywordsPage.countKeywords());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms(synonym),containsInAnyOrder(synonymListCars.toArray()));
		}
	}

	@Test
	public void testAddTwoSynonymsToSynonymGroupFromSearchPage() throws InterruptedException {
		try {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.createSynonymGroup("house home dwelling abode", "English");

			searchPage = getElementFactory().getSearchPage();
			body.getTopNavBar().search("house");

			searchPage.selectLanguage("English", getConfig().getType().getName());

			searchPage.waitForSynonymsLoadingIndicatorToDisappear();
			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(4, createKeywordsPage.countKeywords());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode"));

			searchPage.addSynonymToGroup("lodging", "house");
			searchPage.waitForSynonymsLoadingIndicatorToDisappear();
			assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "lodging"));

			searchPage.addSynonymToGroup("residence", "house");
			searchPage.waitForSynonymsLoadingIndicatorToDisappear();
			assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "lodging", "residence"));

			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			keywordsPage.loadOrFadeWait();
			keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
			assertThat("New synonym has not been added to the group", keywordsPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "lodging", "residence"));

			keywordsPage.deleteKeywords();
			keywordsPage.loadOrFadeWait();

			(new WebDriverWait(getDriver(),10)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					return keywordsPage.countSynonymLists() == 0;
				}
			});

			assertEquals(0, keywordsPage.countSynonymLists());
		} finally {
			getDriver().navigate().refresh();
		}
	}

	@Test
	public void testRemoveTwoSynonymsFromSynonymGroupFromSearchPage() throws InterruptedException {
		try {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.createSynonymGroup("house home dwelling abode residence", "English");

			searchPage = getElementFactory().getSearchPage();
			body.getTopNavBar().search("house");

			searchPage.selectLanguage("English");

			assertEquals(1, searchPage.countSynonymLists());
			assertEquals(5, createKeywordsPage.countKeywords());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "residence"));

			searchPage.deleteSynonym("residence", "house");
			searchPage.loadOrFadeWait();
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("residence")));
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), hasItem("abode"));
			assertThat("More than one synonym deleted", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode"));

			searchPage.deleteSynonym("abode", "house");
			searchPage.loadOrFadeWait();
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("abode")));
			assertThat("More than one synonym deleted", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling"));

			searchPage.deleteSynonym("dwelling", "house");
			searchPage.loadOrFadeWait();
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("dwelling")));
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("abode")));
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("residence")));
			assertThat("More than one synonym deleted", searchPage.getSynonymGroupSynonyms("house"),hasItem("home"));

			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			keywordsPage.loadOrFadeWait();
			keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
			assertThat("Synonyms have not been removed from the group", keywordsPage.getSynonymGroupSynonyms("house"), hasItems("home", "house"));

			keywordsPage.deleteKeywords();		//TODO get deleteAllSynonyms to work again
			keywordsPage.loadOrFadeWait();

			keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

			assertEquals(0, keywordsPage.countSynonymLists());
		} finally {
			getDriver().navigate().refresh();
		}
	}

	@Test
	public void testOnlyLanguagesWithDocumentsAvailableOnSearchPage() throws InterruptedException {
		assertThat("Lanugage not implemented in Hosted",getConfig().getType(),not(ApplicationType.HOSTED));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Baku", "Azeri");

		body.getTopNavBar().search("Baku");
		searchPage = getElementFactory().getSearchPage();
		assertThat(searchPage.getLanguageList(), not(hasItem("Azeri")));
	}

	@Test
	public void testKeywordsLanguage() throws InterruptedException {
		if(getConfig().getType() == ApplicationType.HOSTED){
			fail("Languages not implemented in HOSTED");
		}

		WebDriverWait wait = new WebDriverWait(getDriver(),30);

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Atlanta", "Georgian");

		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Tirana", "Albanian");

		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("Croatia Kroatia Hrvatska", "Croatian");

		if(getConfig().getType().equals(ApplicationType.ON_PREM)){
			wait.until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));
		} else {
			wait.until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		keywordsPage.selectLanguage("Georgian");
		assertEquals(1, keywordsPage.getBlacklistedTerms().size());
		assertEquals(0, keywordsPage.countSynonymLists());

		keywordsPage.selectLanguage("Albanian");
		assertEquals(1, keywordsPage.getBlacklistedTerms().size());
		assertEquals(0, keywordsPage.countSynonymLists());

		keywordsPage.selectLanguage("Croatian");
		assertEquals(0, keywordsPage.getBlacklistedTerms().size());
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.countKeywords());
	}

	@Test
	public void testLanguageOfSearchPageKeywords() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("road rue strasse", "French");
		searchPage = getElementFactory().getSearchPage();
		body.getTopNavBar().search("Korea");
		searchPage.selectLanguage("Chinese");
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.addSynonyms("한국");
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(createKeywordsPage.enabledFinishWizardButton())).click();
		searchPage = getElementFactory().getSearchPage();

		body.getTopNavBar().search("Korea");
		searchPage.selectLanguage("Chinese");
		assertEquals("wrong number of synonym lists displayed", 1, searchPage.countSynonymLists());

		searchPage.selectLanguage("French");
		assertEquals("wrong number of synonym lists displayed", 0, searchPage.countSynonymLists());

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);

		keywordsPage.selectLanguage("French");
		assertThat("synonym assigned to wrong language", keywordsPage.getText(), not(containsString("한국")));

		keywordsPage.selectLanguage("Chinese");
		assertEquals("wrong number of synonym lists displayed", 1, keywordsPage.countSynonymLists());
		assertThat("synonym not assigned to correct language", keywordsPage.getText(), containsString("한국"));
	}

	@Test
	public void testKeywordsCreationAndDeletionOnSecondWindow() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("double duo two pair couple", "Urdu");
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

		keywordsPage.selectLanguage("Urdu");

		keywordsPage.loadOrFadeWait();

		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = keywordsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final KeywordsPage secondKeywordsPage = getElementFactory().getKeywordsPage();
		assertEquals(1, secondKeywordsPage.countSynonymLists());
		assertEquals(5, secondKeywordsPage.countKeywords());

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("couple", "two");

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(1, secondKeywordsPage.countSynonymLists());
		assertEquals(4, secondKeywordsPage.countKeywords());

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("pair", "duo");

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(1, secondKeywordsPage.countSynonymLists());
		assertEquals(3, secondKeywordsPage.countKeywords());
	}

	@Test
	public void testSynonymsNotCaseSensitive() {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();

		createKeywordsPage.addSynonyms("bear");
		assertEquals(1, createKeywordsPage.countKeywords());

		for (final String bearVariant : Arrays.asList("Bear", "beaR", "BEAR", "beAR", "BEar")) {
			createKeywordsPage.addSynonyms(bearVariant);
			assertEquals(1, createKeywordsPage.countKeywords());
			assertThat("bear not included as a keyword", createKeywordsPage.getProspectiveKeywordsList(),hasItem("bear"));
			assertThat("correct error message not showing", createKeywordsPage.getText(), containsString(bearVariant.toLowerCase() + " is a duplicate of an existing keyword."));

		}

		// disallows any adding of synonyms if disallowed synonym found
		createKeywordsPage.addSynonyms("Polar Bear");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("bear not included as a keyword", createKeywordsPage.getProspectiveKeywordsList(), hasItem("bear"));
		assertThat("correct error message not showing", createKeywordsPage.getText(), containsString("bear is a duplicate of an existing keyword."));

		//jam and jaM are case variants so none should be added
		createKeywordsPage.addSynonyms("jam jaM");
		assertEquals(1, createKeywordsPage.countKeywords());
	}


	@Test
	public void testSpinnerPresentOnLastSynonymWhilePenultimateSynonymSpinnerPresent() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("ying yang", "Korean");

		//keywordsPage.selectLanguage("Korean");
		(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for blacklists yet");

		new WebDriverWait(getDriver(),40).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertEquals(1, keywordsPage.countSynonymLists());
		assertEquals(2, keywordsPage.countKeywords());

		keywordsPage.getSynonymIcon("ying", "yang").click();
		if (keywordsPage.getSynonymIcon("ying", "yang").getAttribute("class").contains("fa-spin")) {
			assertThat("Spinner not present on last synonym", keywordsPage.getSynonymIcon("yang", "yang").getAttribute("class"),containsString("fa-spin"));
		}
	}

	@Test
	public void testBooleanTermsNotValidKeyword() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

		//createKeywordsPage.selectLanguage("English");
		(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for blacklists yet");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addSynonyms("holder");
		assertEquals(1, createKeywordsPage.countKeywords());
		final List<String> booleanProximityOperators = Arrays.asList("NOT", "NEAR", "DNEAR", "SOUNDEX", "XNEAR", "YNEAR", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN");

		int operatorsAdded = 1;
		for (final String operator : booleanProximityOperators) {
			createKeywordsPage.addSynonyms(operator);
			assertThat("boolean operator \"" + operator + "\" should not be added as a synonym", createKeywordsPage.getProspectiveKeywordsList(), not(hasItem(operator)));
			assertThat("Operator not added properly. Should be lower case.", createKeywordsPage.getProspectiveKeywordsList(), hasItem(operator.toLowerCase()));
			assertEquals(++operatorsAdded, createKeywordsPage.countKeywords());
//			assertThat("Correct error message not showing", createKeywordsPage.getText(), containsString(operator + " is a boolean or proximity operator. These are invalid"));
//			assertEquals(1, createKeywordsPage.countKeywords());
		}

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		createKeywordsPage.loadOrFadeWait();

		keywordsPage.createNewKeywordsButton().click();

		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST))).click();

		//createKeywordsPage.selectLanguage("English");
		(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for blacklists yet");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addBlacklistedTerms("holder");
		assertEquals(1, createKeywordsPage.countKeywords());

		operatorsAdded = 1;
		for (final String operator : booleanProximityOperators) {
			createKeywordsPage.addBlacklistedTerms(operator);
			assertThat("boolean operator \"" + operator + "\" should not be added as a synonym", createKeywordsPage.getProspectiveKeywordsList(), not(hasItem(operator)));
			assertThat("Operator not added properly. Should be lower case.", createKeywordsPage.getProspectiveKeywordsList(), hasItem(operator.toLowerCase()));
			assertEquals(++operatorsAdded, createKeywordsPage.countKeywords());
//			assertThat("Correct error message not showing", createKeywordsPage.getText(), containsString(operator + " is a boolean or proximity operator. These are invalid"));
//			assertEquals(1, createKeywordsPage.countKeywords());
		}

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		createKeywordsPage.loadOrFadeWait();
	}

	@Test
	public void testAddKeywordsBoxOpenClickDelete() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("бір екі үш төрт бес", "Kazakh");

		new WebDriverWait(getDriver(),40).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

		//keywordsPage.selectLanguage("Kazakh");
		(LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class)).warn("Cannot select language for keywords yet");

		keywordsPage.synonymGroupPlusButton("бір").click();
		assertTrue(keywordsPage.synonymGroupTextBox("бір").isDisplayed());

		keywordsPage.deleteSynonym("екі", "үш");
		assertTrue(keywordsPage.synonymGroupTextBox("бір").isDisplayed());
	}

	@Test
	//CCUK-3243
	//TODO file ticket - Says it has deleted both synonyms but only one has
	public void testQuickSynonymDelete() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("string strong strang streng strung", "German");
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.loadOrFadeWait();

		keywordsPage.selectLanguage("German");

		try {
			keywordsPage.scrollIntoViewAndClick(keywordsPage.getSynonymIcon("strong", "strung"));
			keywordsPage.scrollIntoViewAndClick(keywordsPage.getSynonymIcon("string", "strung"));
		} catch (final WebDriverException w) {
			System.out.println(w);
			fail("Unable to delete a synonym quickly");
		}
		Thread.sleep(15000);
		assertEquals("Incorrect number of synonyms", 1, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.countKeywords());
	}

	@Test
	//CCUK-3245
	public void testAddingForbiddenKeywordsFromUrl() {
		String blacklistUrl = getConfig().getWebappUrl() + "/p/keywords/create/blacklisted/English/";
		String synonymsUrl = getConfig().getWebappUrl() + "/p/keywords/create/synonyms/English/";
		if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
			blacklistUrl = getConfig().getWebappUrl() + "/p/keywords/create/blacklisted/englishUTF8/";
			synonymsUrl = getConfig().getWebappUrl() + "/p/keywords/create/synonyms/englishUTF8/";
		}
		for (final String forbidden : Arrays.asList("(", "\"", "OR")) {
			getDriver().get(blacklistUrl.concat(forbidden));
			keywordsPage.loadOrFadeWait();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			assertThat(forbidden + " is a forbidden keyword and should not be included in the prospective blacklist list", createKeywordsPage.getProspectiveKeywordsList(),not(hasItem("(")));

			getDriver().get(synonymsUrl.concat(forbidden));
			keywordsPage.loadOrFadeWait();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			assertThat(forbidden + " is a forbidden keyword and should not be included in the prospective synonyms list", createKeywordsPage.getProspectiveKeywordsList(),not(hasItem("(")));
		}
	}

	@Test
	public void testSynonymsDisplayedInAlphabeticalOrder() throws InterruptedException {
		for (final String synonyms : Arrays.asList("aa ba ca da", "ab bb cb db", "dc cc bc ac", "ca ba da aa")) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.createSynonymGroup(synonyms, "English");

			searchPage = getElementFactory().getSearchPage();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			final List<String> keywords = searchPage.getLeadSynonymsList();

			for (int i = 0; i < keywords.size() - 1; i++) {
				assertTrue(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0);
			}
			searchPage.loadOrFadeWait();
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage.loadOrFadeWait();
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.searchFilterTextBox().sendKeys("cc");
		final List<String> keywords = keywordsPage.getLeadSynonymsList();

		for (int i = 0; i < keywords.size() - 1; i++) {
			assertTrue(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0);
		}
	}

	@Test
	public void testBlacklistedKeywordsDisplayedInAlphabeticalOrder() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");

		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);

		Thread.sleep(10000);    //TODO Need to find a better way to do this -- need to wait for all blacklist terms to be added

		final List<String> keywords = keywordsPage.getBlacklistedTerms();
		assertEquals("Wrong number of blacklist items created", 8, keywords.size());

		for (int i = 0; i < keywords.size() - 1; i++) {
			assertTrue(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0);
		}
	}

	@Test
	public void testDeletingOfSynonymsAndBlacklistedTerms() throws InterruptedException {
		WebDriverWait wait = new WebDriverWait(getDriver(),10);

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");

		Thread.sleep(15000); //Wait for all blacklist terms to be added TODO nicer way

		keywordsPage.createNewKeywordsButton(wait).click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");
		searchPage = getElementFactory().getSearchPage();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		keywordsPage.deleteSynonym("es", "es");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteSynonym("ea", "ea");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteSynonym("eg", "eg");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteSynonym("ed", "ed");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteSynonym("ef", "ef");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteKeywords();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");

		Thread.sleep(15000); //Wait for all blacklist terms to be added TODO nicer way

		keywordsPage.createNewKeywordsButton(wait).click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");

		new WebDriverWait(getDriver(),40).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		keywordsPage.deleteBlacklistedTerm("db");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteBlacklistedTerm("aa");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());

		keywordsPage.deleteBlacklistedTerm("da");
		assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());
	}

	@Test
	public void testDoesDeletingSynonymDisableOtherSynonyms() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		//Wait for the synonyms to be added
		new WebDriverWait(getDriver(),45).until(new WaitForNotification("Created a new synonym group containing: "));

		final List<String> synonyms = Arrays.asList("ea", "es", "ed", "ef", "eg");
		for (final String synonym : synonyms) {
			keywordsPage.getSynonymIcon(synonym, synonym).click();

			if(getParent(getParent(getParent(keywordsPage.getSynonymIcon(synonym)))).findElements(By.tagName("li")).size() > 2){
				assertEquals("Too many synonyms are disabled on synonym delete", 1, keywordsPage.countRefreshIcons());
			} else {
				assertEquals(2,keywordsPage.countRefreshIcons());
			}

			//Wait for deletion to complete
			new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return keywordsPage.countRefreshIcons() == 0;
				}
			});

			assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());
		}
	}

	@Test
	public void testAllowKeywordStringsThatContainBooleansWithinThem() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addSynonyms("placeholder");
		assertEquals(1, createKeywordsPage.getProspectiveKeywordsList().size());

		final List<String> hiddenSearchOperators = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "NEARLY", "NEAR123LY", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");

		for (int i = 0; i < hiddenSearchOperators.size(); i++) {
			createKeywordsPage.addSynonymsTextBox().clear();
			createKeywordsPage.addSynonymsTextBox().sendKeys(hiddenSearchOperators.get(i));
			createKeywordsPage.addSynonymsButton().click();
			createKeywordsPage.loadOrFadeWait();
			assertEquals(2 + i, createKeywordsPage.getProspectiveKeywordsList().size());
		}
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		keywordsPage.loadOrFadeWait();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addBlacklistedTerms("placeholder");
		assertEquals(1, createKeywordsPage.getProspectiveKeywordsList().size());

		for (int i = 0; i < hiddenSearchOperators.size(); i++) {
			createKeywordsPage.addBlacklistedTextBox().clear();
			createKeywordsPage.addBlacklistedTextBox().sendKeys(hiddenSearchOperators.get(i));
			createKeywordsPage.addBlacklistTermsButton().click();
			createKeywordsPage.loadOrFadeWait();
			assertEquals(2 + i, createKeywordsPage.getProspectiveKeywordsList().size());
		}

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		keywordsPage.loadOrFadeWait();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("place holder", "English");
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();

		keywordsPage.selectLanguage("English");

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

		for (final String hiddenBooleansProximity : hiddenSearchOperators) {
			LoggerFactory.getLogger(KeywordsPageAndWizardITCase.class).info("Adding '"+hiddenBooleansProximity+"'");

			keywordsPage.addSynonymToGroup(hiddenBooleansProximity, "holder");

			new WebDriverWait(getDriver(),120).until(new ExpectedCondition<Boolean>() {     //This is too long but after sending lots of requests it slows down a loto
				@Override
				public Boolean apply(WebDriver webDriver) {
					return keywordsPage.synonymGroupTextBox("holder").isEnabled();
				}
			});

			keywordsPage.loadOrFadeWait();
			assertEquals(1, keywordsPage.countSynonymGroupsWithSynonym(hiddenBooleansProximity.toLowerCase()));
		}
	}

	@Test
	public void testAddingSynonymGroupFromSearchPageOnlyAddsWords(){
		String phrase = "the quick brown fox jumps over the lazy dog";
		body.getTopNavBar().search(phrase);
		searchPage = getElementFactory().getSearchPage();
		searchPage.selectLanguage("English", getConfig().getType().getName());

		searchPage.createSynonymsLink().click();

		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

		ArrayList<String> wordsInPhrase = new ArrayList<String>(Arrays.asList(phrase.split(" ")));
		wordsInPhrase.removeAll(Collections.singleton("the"));

		List<String> prospectiveKeywords = createKeywordsPage.getProspectiveKeywordsList();

		assertThat(prospectiveKeywords, containsItems(wordsInPhrase));
		assertThat(prospectiveKeywords, not(hasItem("the")));
		assertEquals(wordsInPhrase.size(),prospectiveKeywords.size());
	}

	@Test
	public void testSynonymNotificationText() throws InterruptedException {
		String synonymOne = "Flesh";
		String synonymTwo = "Meat";
		String synonymThree = "Skin";

		String[] synonyms = new String[]{synonymOne, synonymTwo, synonymThree};
		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup(join(synonyms, ' '), "English");
		Arrays.sort(synonyms);
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		new WebDriverWait(getDriver(),30).until(new WaitForNotification("Created a new synonym group containing: "));
		assertThat(notifications.notificationNumber(1).getText(),is("Created a new synonym group containing: " + synonyms[0].toLowerCase() + ", " + synonyms[1].toLowerCase() + ", " + synonyms[2].toLowerCase()));
	}

	@Test
	public void testBlacklistNotificationText() throws InterruptedException {
		String blacklistOne = "Aardvark";
		String blacklistTwo = "Aardwolf";

		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm(blacklistOne + " " + blacklistTwo, "English");
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		new WebDriverWait(getDriver(),30).until(new WaitForNotification("blacklist"));
		assertThat(notifications.notificationNumber(1).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(2).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(1).getText(), not(notifications.notificationNumber(2).getText()));
	}

	@Test
	public void testClickingOnNotifications() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup("a b c d", "English");
		getElementFactory().getSearchPage();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		getElementFactory().getPromotionsPage();
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();

		verifyThat(notifications.notificationNumber(1).getText(), is("Created a new synonym group containing: a, b, c, d"));

		notifications.notificationNumber(1).click();

		verifyThat(getDriver().getCurrentUrl(),containsString("keywords"));

		if(!getDriver().getCurrentUrl().contains("keywords")){
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm("e", "English");
		getElementFactory().getKeywordsPage();

		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);

			((HSOElementFactory) getElementFactory()).getAnalyticsPage();
			body = getBody();

			new WebDriverWait(getDriver(), 30).until(new ExpectedCondition<Boolean>() {

				@Override
				public Boolean apply(WebDriver driver) {
					return !driver.findElement(By.xpath("//nav[not(contains(@class, 'affix-clone'))]//*[@id='navbar-bell']/span")).getAttribute("class").contains("ng-hide");
				}
			});
		} else {
			body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		}

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();

		verifyThat(notifications.notificationNumber(1).getText(), is("Added \"e\" to the blacklist"));

		notifications.notificationNumber(1).click();

		verifyThat(getDriver().getCurrentUrl(),containsString("keywords"));

	}

	/**
	 * The assumed behaviour for where a blacklisted item is also within the synonym group is as follows:
	 *
	 * 		- Searching for the blacklisted term will bring up no results
	 * 		- Searching for the other terms within the synonym group will bring up results for ALL terms within the synonym group INCLUDING the blacklisted term
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testOverlappingBlacklistSynonym() throws InterruptedException {
		String blacklist = "blacklist";
		String synonym = "synonym";
		String[] synonymGroup = new String[]{blacklist, synonym};

		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup(join(synonymGroup,' '),"English");
		getElementFactory().getSearchPage();
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm(blacklist,"English");
		getElementFactory().getKeywordsPage();

		body.getTopNavBar().search(blacklist);

		searchPage = getElementFactory().getSearchPage();

		//Make sure no results show up for blacklisted terms
		assertThat(searchPage.visibleDocumentsCount(),is(0));

		body.getTopNavBar().search(synonym);

		searchPage = getElementFactory().getSearchPage();

		//Make sure some results show up for terms within the synonym group
		assertThat(searchPage.visibleDocumentsCount(),not(0));
	}

}