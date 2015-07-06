package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.NotificationsDropDown;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;


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
		navBar.switchPage(NavBarTabId.KEYWORDS);
		navBar.loadOrFadeWait();
		keywordsPage = body.getKeywordsPage();
	}

	@Test
	public void testCreateNewKeywordsButtonAndCancel() {
		assertThat("Create new keywords button is not visible", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));
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
		try {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = body.getCreateKeywordsPage();
			assertThat("Continue button should be disabled until a keywords type is selected", keywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE), "disabled"));

			createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
			assertThat("Synonym type not set active", createKeywordsPage.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM)).getAttribute("class").contains("progressive-disclosure-selection"));
			assertThat("Continue button should be enabled", !createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).getAttribute("class").contains("disabled"));
			assertThat("languages select should be visible", createKeywordsPage.languagesSelectBox().isDisplayed());

			createKeywordsPage.selectLanguage("French");
			assertEquals("French", createKeywordsPage.languagesSelectBox().getText());
			createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
			createKeywordsPage.loadOrFadeWait();
			assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

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

			createKeywordsPage.finishWizardButton().click();
			createKeywordsPage.loadOrFadeWait();
			searchPage = body.getSearchPage();
			new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

			searchPage.selectLanguage("French", getConfig().getType().getName());
			new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
			final List<String> searchTerms = searchPage.getSearchTermsList();
			assertThat("Synonym group does not contain 'stuff', 'horse', 'pony' and 'things'", searchTerms.containsAll(Arrays.asList("stuff", "horse", "pony", "things")));

			new SideNavBar(getDriver()).getTab(NavBarTabId.KEYWORDS).click();
			new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
			keywordsPage.selectLanguage("French");
			assertThat("synonym horse is not displayed", keywordsPage.leadSynonym("horse").isDisplayed());

			final List<String> synonymGroup = keywordsPage.getSynonymGroupSynonyms("horse");
			assertThat("Synonym group does not contain 'stuff', 'horse', 'pony' and 'things'", synonymGroup.containsAll(Arrays.asList("stuff", "horse", "pony", "things")));
		} finally {
			new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
			keywordsPage.deleteAllSynonyms();
		}
	}

	@Test
	public void testWizardCancelButtonsWorksAfterClickingTheNavBarToggleButton() {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateBlacklistedWizard() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("Continue button should be disabled until a keywords type is selected", createKeywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE), "disabled"));

		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		assertThat("Blacklisted type not set active", createKeywordsPage.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST)).getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", !createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).getAttribute("class").contains("disabled"));

		assertThat("Wizard did not navigate to languages page", createKeywordsPage.languagesSelectBox().isDisplayed());

		createKeywordsPage.selectLanguage("Swahili");
		assertEquals("Swahili", createKeywordsPage.languagesSelectBox().getText());

		createKeywordsPage.selectLanguage("English");
		assertEquals("English", createKeywordsPage.languagesSelectBox().getText());

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

		createKeywordsPage.finishWizardButton().click();
		createKeywordsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		final List<String> blacklistTerms = keywordsPage.getBlacklistedTerms();
		createKeywordsPage.loadOrFadeWait();
		assertThat("blacklist terms not all displayed", blacklistTerms.containsAll(Arrays.asList("danger", "warning", "beware", "scary")));
		assertThat("too many blacklist terms", blacklistTerms.size() == 4);
	}

	@Test
	public void testKeywordsFilter() throws InterruptedException {
		final WebDriverWait wait = new WebDriverWait(getDriver(), 5);
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("dog hound canine", "English");

		searchPage = body.getSearchPage();
		wait.until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText().contains("dog"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText().contains("hound"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText().contains("canine"));

		new SideNavBar(getDriver()).getTab(NavBarTabId.KEYWORDS).click();
		wait.until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		keywordsPage.selectLanguage("English");
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog").containsAll(Arrays.asList("hound", "canine")));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound").containsAll(Arrays.asList("dog", "canine")));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine").containsAll(Arrays.asList("dog", "hound")));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("illegal", "English");
		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms().contains("illegal"));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertThat("Blacklist terms are still visible", keywordsPage.getBlacklistedTerms().size() == 0);
		assertThat("A synonym list on row 2 is not visible", keywordsPage.synonymList(1).isDisplayed());
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog").containsAll(Arrays.asList("hound", "canine")));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound").containsAll(Arrays.asList("dog", "canine")));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine").containsAll(Arrays.asList("dog", "hound")));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);
		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms().contains("illegal"));
		assertEquals("There should not be a a synonym list on row 2", 0, keywordsPage.countSynonymLists());

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		assertThat("A synonym list should be visible on row 2", keywordsPage.synonymList(1).isDisplayed());
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog").containsAll(Arrays.asList("hound", "canine")));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound").containsAll(Arrays.asList("dog", "canine")));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine").containsAll(Arrays.asList("dog", "hound")));
		assertThat("Blacklist term illegal is not visible", keywordsPage.getBlacklistedTerms().contains("illegal"));
	}

	@Test
	public void testDeleteKeywords() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("frog toad amphibian tadpole", "English");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("frog").containsAll(Arrays.asList("frog", "toad", "amphibian", "tadpole")));
		assertEquals("Wrong number of synonym lists displayed", 4, keywordsPage.countSynonymLists());
		assertEquals("Wrong number of synonyms in group frog", 4, keywordsPage.getSynonymGroupSynonyms("frog").size());

		keywordsPage.deleteSynonym("amphibian", "toad");
		assertEquals("Wrong number of synonym lists displayed", 3, keywordsPage.countSynonymLists());
		assertEquals("Wrong number of synonyms in group toad", 3, keywordsPage.getSynonymGroupSynonyms("toad").size());
		assertThat("the synonym amphibian should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("tadpole").contains("amphibian"));
		assertThat("the synonym amphibian should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("toad").contains("amphibian"));
		assertThat("the synonym amphibian should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("frog").contains("amphibian"));

		keywordsPage.deleteSynonym("frog", "frog");
		assertEquals("Wrong number of synonym lists displayed", 2, keywordsPage.countSynonymLists());
		assertEquals("Wrong number of synonyms in group toad", 2, keywordsPage.getSynonymGroupSynonyms("toad").size());
		assertThat("the synonym frog should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("toad").contains("frog"));
		assertThat("the synonym frog should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("tadpole").contains("frog"));

		keywordsPage.deleteSynonym("tadpole", "toad");
		assertEquals("Wrong number of synonym lists displayed", 0, keywordsPage.countSynonymLists());
	}

	//The keyword 'wine' exists in two different synonym groups. Tests that deleting this keyword does not effect the other synonym group
	@Test
	public void testDeleteSynonymsFromOverlappingSynonymGroups() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("wine merlot shiraz bordeaux", "English");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("wine red scarlet burgundy", "English");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("red").containsAll(Arrays.asList("red", "scarlet", "wine", "burgundy")));
		assertEquals(8, keywordsPage.countSynonymLists());
		assertEquals(4, keywordsPage.getSynonymGroupSynonyms("burgundy").size());
		assertEquals(4, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(2, keywordsPage.countSynonymGroupsWithLeadSynonym("wine"));

		keywordsPage.deleteSynonym("bordeaux", "shiraz");
		assertEquals(7, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(4, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(2, keywordsPage.countSynonymGroupsWithLeadSynonym("wine"));

		keywordsPage.deleteSynonym("burgundy", "red");
		assertEquals(6, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(2, keywordsPage.countSynonymGroupsWithLeadSynonym("wine"));

		keywordsPage.deleteSynonym("wine", "red");
		assertEquals(5, keywordsPage.countSynonymLists());
		assertEquals(3, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(1, keywordsPage.countSynonymGroupsWithLeadSynonym("wine"));

		keywordsPage.deleteSynonym("shiraz", "wine");
		assertEquals(4, keywordsPage.countSynonymLists());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("scarlet").size());
		assertEquals(1, keywordsPage.countSynonymGroupsWithLeadSynonym("wine"));

		keywordsPage.deleteSynonym("scarlet", "red");
		assertEquals(2, keywordsPage.countSynonymLists());
		assertEquals(2, keywordsPage.getSynonymGroupSynonyms("merlot").size());
		assertEquals(1, keywordsPage.countSynonymGroupsWithLeadSynonym("wine"));

		keywordsPage.deleteSynonym("wine", "merlot");
		assertEquals(0, keywordsPage.countSynonymLists());
	}

	//Duplicate blacklisted terms are not allowed to be created within the same language
	@Test
	public void testCreateDuplicateBlacklist() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("fish", "English");
		assertThat("Blacklist fish not visible", keywordsPage.getBlacklistedTerms().contains("fish"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		createKeywordsPage.selectLanguage("English");
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("fish");
		createKeywordsPage.addBlacklistTermsButton().click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Duplicate blacklist warning message not present", createKeywordsPage.getText().contains("The word \"fish\" is already blacklisted"));
		assertThat("Duplicate blacklist term should not be added", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().clear();
		createKeywordsPage.addBlacklistedTextBox().sendKeys("chips");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Duplicate blacklist warning message has not disappeared", !createKeywordsPage.getText().contains("The word \"fish\" is already blacklisted"));
		assertThat("New blacklist term should be added", createKeywordsPage.countKeywords() == 1);
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.deleteKeyword("chips");
		assertThat("There should be no blacklist terms", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		assertThat("Cancel button redirects to wrong page", getDriver().getCurrentUrl().endsWith("keywords"));
		assertEquals("Wrong number of blacklisted terms", 1, keywordsPage.getBlacklistedTerms().size());
	}

	//Whitespace of any form should not be added as a blacklisted term
	@Test
	public void testWhitespaceBlacklistTermsWizard() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
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
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
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
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
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
		assertThat("Correct error message not showing", createKeywordsPage.getText().contains("Terms have an odd number of quotes, suggesting an unclosed phrase"));

		createKeywordsPage.addSynonyms("\"closed phrase\"");
		assertEquals(2, createKeywordsPage.countKeywords());
		assertThat("Phrase not created", createKeywordsPage.getProspectiveKeywordsList().contains("closed phrase"));
		assertThat("Quotes unescaped", !createKeywordsPage.getProspectiveKeywordsList().contains("/"));
	}

	//Odd number of quotes or quotes with blank text should not be able to be added as a blacklisted term
	@Test
	public void testQuotesInBlacklistWizard() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		createKeywordsPage.selectLanguage("English");
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
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("d\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("\"d\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("s\"d\"d");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

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
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("d\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("\"d\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));

		createKeywordsPage.addBlacklistedTerms("s\"d\"d");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("wrong/no error message", createKeywordsPage.getText().contains("Terms may not contain quotation marks"));
	}

	@Ignore // This takes too long for a nightly test but is a useful test that need run periodically as the application has failed in the past with a large number of synonym groups.
	// Failure can present itself on other pages other than the KeywordsPage
	@Test
	public void testAddLotsOfSynonymGroups() throws IOException, InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		final List<String> groupsOfFiveSynonyms = keywordsPage.loadTextFileLineByLineIntoList("C://dev//res//100.txt");

		for (final String synonymGroup : groupsOfFiveSynonyms) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = body.getCreateKeywordsPage();
			createKeywordsPage.createSynonymGroup(synonymGroup, "English");

			new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
			assertThat("Wrong number of synonym lists", keywordsPage.countSynonymLists() == groupsOfFiveSynonyms.indexOf(synonymGroup) + 1);
		}
	}

	//Whitespace, Odd number of quotes or quotes with blank text, boolean operators or proximity operators should not be able to added as keywords. This test checks they can't be added to existing synonyms on the Keywords Page
	@Test
	public void testAddingWhitespaceQuotesBooleansProximityOperatorsOnKeywordsPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("one two three", "English");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		keywordsPage.addSynonymToGroup("four", "one");
		keywordsPage.waitForRefreshIconToDisappear();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two").contains("four"));
		assertEquals(4, keywordsPage.countSynonymLists());

		keywordsPage.synonymGroupPlusButton("three").click();
		keywordsPage.synonymGroupTextBox("three").clear();
		keywordsPage.synonymGroupTickButton("three").click();
		assertThat("add synonym box should still be displayed", keywordsPage.synonymGroupTextBox("three").isDisplayed());

		keywordsPage.searchFilterTextBox().click();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two").contains("four"));
		assertEquals(4, keywordsPage.countSynonymLists());

		for (final String badSynonym : Arrays.asList(" ", "\t", "\"", "NOT", "NEAR", "DNEAR", "XNEAR", "YNEAR", "NEAR123", "SENTENCE2", "PARAGRAPH3", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN")) {
			keywordsPage.synonymGroupPlusButton("three").click();
			keywordsPage.synonymGroupTextBox("three").clear();
			keywordsPage.synonymGroupTextBox("three").sendKeys(badSynonym);
			keywordsPage.synonymGroupTickButton("three").click();
			assertThat("add synonym box should still be displayed. Offending term is " + badSynonym, keywordsPage.synonymGroupTextBox("three").isDisplayed());

			keywordsPage.loadOrFadeWait();
			keywordsPage.searchFilterTextBox().click();
			assertThat("there should be four synonyms in a group. Offending term is " + badSynonym, keywordsPage.getSynonymGroupSynonyms("one").size() == 4);
			assertEquals(4, keywordsPage.countSynonymLists());
		}
	}

	//Phrases should be able to be added as synonyms from the keywords page
	@Test
	public void testPhrasesCanBeAddedAsSynonymsOnKeywordsPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("one two three", "English");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		keywordsPage.synonymGroupPlusButton("three").click();
		keywordsPage.synonymGroupTextBox("three").clear();
		keywordsPage.synonymGroupTextBox("three").sendKeys("four and five");
		keywordsPage.synonymGroupTickButton("three").click();
		Thread.sleep(5000);
		assertFalse(keywordsPage.synonymGroupTextBox("three").isDisplayed());
		assertEquals(4, keywordsPage.countSynonymLists());
		assertTrue(keywordsPage.getSynonymGroupSynonyms("three").contains("four and five"));
	}

	//Notification should drop down on creation of new keywords
	@Test
	public void testNotificationForCreatedBlacklistedTermAndSynonymGroup() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("orange", "English");
		body.waitForGritterToClear();
		new SideNavBar(getDriver()).switchPage(NavBarTabId.PROMOTIONS);

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Added \"orange\" to the blacklist"));

		notifications = body.getNotifications();
		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("piano keyboard pianoforte", "English");
		body.waitForGritterToClear();

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("keyboard", "piano");
		body.waitForGritterToClear();
		new SideNavBar(getDriver()).switchPage(NavBarTabId.PROMOTIONS);

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Updated a synonym group containing: piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText().contains("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(3).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);
		keywordsPage.selectLanguage("English");
		keywordsPage.deleteBlacklistedTerm("orange");
		body.waitForGritterToClear();
		new SideNavBar(getDriver()).switchPage(NavBarTabId.OVERVIEW);

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Removed \"orange\" from the blacklist"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText().contains("Updated a synonym group containing: piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(3).getText().contains("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(4).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));
	}

	// This only tests the notifications dropdown and not the gritters
	@Test
	public void testHTMLEscapedInNotifications() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("<h1>Hi</h1>", "English");
		body.waitForGritterToClear();
		new SideNavBar(getDriver()).switchPage(NavBarTabId.PROMOTIONS);

		topNavBar.notificationsDropdown();
		notifications = body.getNotifications();
		assertThat("Notification text incorrect, HTML not escaped", !notifications.notificationNumber(1).getText().contains("Added \"Hi\" to the blacklist"));
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Added \"<h1>Hi</h1>\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));
	}

	//Blacklisted terms can be created on the searchpage. This link has often broken
	@Test
	public void testCreateBlacklistedTermFromSearchPage() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("noir");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("French", getConfig().getType().getName());
		searchPage.loadOrFadeWait();

		assertThat("No results for search noir", searchPage.docLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.blacklistLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to blacklist wizard", getDriver().getCurrentUrl().contains("keywords/create"));
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("link not directing to blacklist wizard", createKeywordsPage.getText().contains("Select terms to blacklist"));
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("noir");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

		createKeywordsPage.finishWizardButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("Blacklisted term not added", keywordsPage.getBlacklistedTerms().contains("noir"));
	}

	//There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current search term. Often breaks.
	@Test
	public void testCreateSynonymGroupFromSearchPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("rouge");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("French", getConfig().getType().getName());

		assertThat("No results for search rouge", searchPage.docLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl().contains("keywords/create"));
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("link not directing to synonym group wizard", createKeywordsPage.getText().contains("Select synonyms"));
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList().contains("rouge"));
		assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("rouge");
		createKeywordsPage.addSynonymsButton().click();
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList().contains("rouge"));
		assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().clear();
		createKeywordsPage.addSynonymsTextBox().sendKeys("red");
		createKeywordsPage.addSynonymsButton().click();
		assertEquals(2, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList().contains("rouge"));
		assertThat("keywords list does not include term 'red'", createKeywordsPage.getProspectiveKeywordsList().contains("red"));
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.finishWizardButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(keywordsPage.selectLanguageButton()));
		assertEquals("Blacklist has been created in the wrong language", "French", keywordsPage.getSelectedLanguage());
		keywordsPage.selectLanguage("French");
		assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("rouge").contains("red"));
		assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("red").contains("rouge"));
		assertEquals(2, keywordsPage.countSynonymLists());
	}

	//There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current multi term search. Often breaks.
	@Test
	public void testCreateSynonymGroupFromMultiTermSearchOnSearchPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("lodge dodge podge");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("English", getConfig().getType().getName());

		assertThat("No results for search", searchPage.docLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl().contains("keywords/create"));
		createKeywordsPage = body.getCreateKeywordsPage();
		assertThat("link not directing to synonym group wizard", createKeywordsPage.getText().contains("Select synonyms"));
		assertEquals(3, createKeywordsPage.countKeywords());
		assertThat("Wrong prospective blacklisted terms added", createKeywordsPage.getProspectiveKeywordsList().containsAll(Arrays.asList("lodge", "dodge", "podge")));
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

		createKeywordsPage.finishWizardButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("lodge").containsAll(Arrays.asList("lodge", "dodge", "podge")));
		assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("podge").containsAll(Arrays.asList("lodge", "dodge", "podge")));
		assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("dodge").containsAll(Arrays.asList("lodge", "dodge", "podge")));

		assertEquals(3, keywordsPage.countSynonymLists());
	}

	@Test
	public void testKeywordsSearchFilter() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("grizzly brownBear bigBear", "English");
		//All keywords should be changed by the application to lowercase in all instances
		final List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
		new WebDriverWait(getDriver(), 15).until(ExpectedConditions.visibilityOf(body.getSearchPage()));
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title().contains(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor().contains(synonym));
			assertThat(synonym + " synonym group not complete in'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertEquals("Wrong number of synonym lists displayed", 3, searchPage.countSynonymLists());
		}

		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("honeyBee bumbleBee buzzyBee", "English");
		final List<String> synonymListBees = Arrays.asList("honeybee", "bumblebee", "buzzybee");
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListBees) {
			assertThat(synonym + " not included in title", searchPage.title().contains(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor().contains(synonym));
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBees));
			assertEquals(3, searchPage.countSynonymLists());
		}

		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("English");
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertEquals(6, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("zz");
		assertEquals(6, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("buzz");
		keywordsPage.loadOrFadeWait();
		assertEquals(3, keywordsPage.countSynonymLists());

		for (final String synonym : synonymListBees) {
			assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBees));
			assertThat("synonym included in synonym group: " + synonym + " that should not be there", !keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("a");
		keywordsPage.searchFilterTextBox().sendKeys(Keys.BACK_SPACE);
		keywordsPage.loadOrFadeWait();
		assertEquals(6, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("Bear");
		keywordsPage.loadOrFadeWait();
		assertEquals(3, keywordsPage.countSynonymLists());

		for (final String synonym : synonymListBears) {
			assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertThat("synonym included in synonym group: " + synonym + " that should not be there", !keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBees));
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("a");
		keywordsPage.searchFilterTextBox().sendKeys(Keys.BACK_SPACE);
		keywordsPage.loadOrFadeWait();
		assertEquals(6, keywordsPage.countSynonymLists());

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymList));
			}
		}
	}

	@Test
	public void testSearchPageKeywords() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
		createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListBears, ' '), "English");
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title().contains(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor().contains(synonym));
			assertThat(synonym + " synonym group not complete in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertEquals(3, searchPage.countSynonymLists());
		}

		searchPage.addSynonymToGroup("kodiak", "grizzly");
		searchPage.loadOrFadeWait();
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym).contains("kodiak"));
			assertEquals(3, searchPage.countSynonymLists());
		}

		searchPage.deleteSynonym("bigbear", "brownbear");
		searchPage.loadOrFadeWait();
		synonymListBears = Arrays.asList("grizzly", "brownbear");
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertThat("bigbear not deleted from group " + synonym, !searchPage.getSynonymGroupSynonyms(synonym).contains("bigbear"));
			assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym).contains("kodiak"));
			assertEquals(2, searchPage.countSynonymLists());
		}

		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("English");
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertEquals(3, keywordsPage.countSynonymLists());

		synonymListBears = Arrays.asList("grizzly", "brownbear", "kodiak");
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " group incomplete", keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertEquals(3, keywordsPage.getSynonymGroupSynonyms(synonym).size());
			assertThat("bigbear not deleted from group " + synonym, !keywordsPage.getSynonymGroupSynonyms(synonym).contains("bigbear"));
		}
	}

	@Test
	public void testNoBlacklistLinkForBlacklistedSearch() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("wizard");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("Arabic", getConfig().getType().getName());

		searchPage.blacklistLink().click();
		try {
			createKeywordsPage = body.getCreateKeywordsPage();
			createKeywordsPage.finishWizardButton().click();
		} catch (final NoSuchElementException e) {
			fail("blacklist link on search page has not navigated to the wizard");
		}
		new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(keywordsPage.selectLanguageButton()));
		assertEquals("Blacklist has been created in the wrong language", "Arabic", keywordsPage.getSelectedLanguage());
		keywordsPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);
		keywordsPage.selectLanguage("Arabic");
		assertThat("Blacklisted term not created", keywordsPage.getBlacklistedTerms().contains("wizard"));

		topNavBar.search("wizard");
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		searchPage.selectLanguage("Arabic", getConfig().getType().getName());

		assertThat("'You searched for:' section incorrect", searchPage.youSearchedFor().contains("wizard"));
		assertThat("Keywords incorrect", searchPage.getBlacklistedTerms().contains("wizard"));
		assertThat("link to blacklist or create synonyms should not be present", !searchPage.getText().contains("You can create synonyms or blacklist these search terms"));

		searchPage.selectLanguage("English", getConfig().getType().getName());
		assertThat("Term should not be blacklisted in English", !searchPage.getText().contains("Any query terms were either blacklisted or stop words"));
	}

	@Test
	public void testSynonymGroupMembersSearchWholeGroup() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		final List<String> synonymListCars = Arrays.asList("car", "auto", "motor");
		createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListCars, ' '), "Swahili");
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListCars) {
			topNavBar.search(synonym);
			searchPage.selectLanguage("Swahili", getConfig().getType().getName());
			assertEquals(1, searchPage.countSynonymLists());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListCars));
		}
	}

	@Test
		 public void testAddTwoSynonymsToSynonymGroupFromSearchPage() throws InterruptedException {
		try {
			keywordsPage.deleteAllSynonyms();
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = body.getCreateKeywordsPage();
			createKeywordsPage.createSynonymGroup("house home dwelling abode", "English");

			searchPage = body.getSearchPage();
			topNavBar.search("house");
			searchPage.selectLanguage("English", getConfig().getType().getName());
			assertEquals(1, searchPage.countSynonymLists());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling", "abode")));

			searchPage.addSynonymToGroup("lodging", "house");
			searchPage.loadOrFadeWait();
			assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling", "abode", "lodging")));

			searchPage.addSynonymToGroup("residence", "house");
			searchPage.loadOrFadeWait();
			assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling", "abode", "lodging", "residence")));

			new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
			keywordsPage.loadOrFadeWait();
			assertThat("New synonym has not been added to the group", keywordsPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling", "abode", "lodging", "residence")));

			keywordsPage.deleteAllSynonyms();
			keywordsPage.loadOrFadeWait();
			assertEquals(0, keywordsPage.countSynonymLists());
		} finally {
			getDriver().navigate().refresh();
		}
	}

	@Test
	public void testRemoveTwoSynonymsFromSynonymGroupFromSearchPage() throws InterruptedException {
		try {
			keywordsPage.deleteAllSynonyms();
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = body.getCreateKeywordsPage();
			createKeywordsPage.createSynonymGroup("house home dwelling abode residence", "English");

			searchPage = body.getSearchPage();
			topNavBar.search("house");
			searchPage.selectLanguage("English", getConfig().getType().getName());
			assertEquals(1, searchPage.countSynonymLists());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling", "abode", "residence")));

			searchPage.deleteSynonym("residence", "house");
			searchPage.loadOrFadeWait();
			assertThat("Synonym has not been deleted", !searchPage.getSynonymGroupSynonyms("house").contains("residence"));
			assertThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house").contains("abode"));
			assertThat("More than one synonym deleted", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling", "abode")));

			searchPage.deleteSynonym("abode", "house");
			searchPage.loadOrFadeWait();
			assertThat("Synonym has not been deleted", !searchPage.getSynonymGroupSynonyms("house").contains("abode"));
			assertThat("More than one synonym deleted", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "dwelling")));

			searchPage.deleteSynonym("dwelling", "house");
			searchPage.loadOrFadeWait();
			assertThat("Synonym has not been deleted", !searchPage.getSynonymGroupSynonyms("house").contains("dwelling"));
			assertThat("Synonym has not been deleted", !searchPage.getSynonymGroupSynonyms("house").contains("abode"));
			assertThat("Synonym has not been deleted", !searchPage.getSynonymGroupSynonyms("house").contains("residence"));
			assertThat("More than one synonym deleted", searchPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home")));

			new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
			keywordsPage.loadOrFadeWait();
			assertThat("Synonyms have not been removed from the group", keywordsPage.getSynonymGroupSynonyms("house").containsAll(Arrays.asList("home", "house")));

			keywordsPage.deleteAllSynonyms();
			keywordsPage.loadOrFadeWait();
			assertEquals(0, keywordsPage.countSynonymLists());
		} finally {
			getDriver().navigate().refresh();
		}
	}

	@Test
	public void testOnlyLanguagesWithDocumentsAvailableOnSearchPage() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Baku", "Azeri");

		topNavBar.search("Baku");
		searchPage = body.getSearchPage();
		assertFalse(searchPage.getLanguageList().contains("Azeri"));
	}

	@Test
	public void testKeywordsLanguage() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Atlanta", "Georgian");

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Tirana", "Albanian");

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("Croatia Kroatia Hrvatska", "Croatian");
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
		keywordsPage.selectLanguage("Georgian");
		assertEquals(1, keywordsPage.getBlacklistedTerms().size());
		assertEquals(0, keywordsPage.countSynonymLists());

		keywordsPage.selectLanguage("Albanian");
		assertEquals(1, keywordsPage.getBlacklistedTerms().size());
		assertEquals(0, keywordsPage.countSynonymLists());

		keywordsPage.selectLanguage("Croatian");
		assertEquals(0, keywordsPage.getBlacklistedTerms().size());
		assertEquals(3, keywordsPage.countSynonymLists());
	}

	@Test
	public void testLanguageOfSearchPageKeywords() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("road rue strasse", "French");
		topNavBar.search("Korea");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("Chinese", getConfig().getType().getName());
		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.addSynonyms("한국");
		createKeywordsPage.finishWizardButton().click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		topNavBar.search("Korea");
		searchPage.selectLanguage("Chinese", getConfig().getType().getName());
		assertEquals("wrong number of synonym lists displayed", 1, searchPage.countSynonymLists());

		searchPage.selectLanguage("French", getConfig().getType().getName());
		assertEquals("wrong number of synonym lists displayed", 0, searchPage.countSynonymLists());

		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.selectLanguage("French");
		assertThat("synonym assigned to wrong language", !keywordsPage.getText().contains("한국"));

		keywordsPage.selectLanguage("Chinese");
		assertEquals("wrong number of synonym lists displayed", 2, keywordsPage.countSynonymLists());
		assertThat("synonym not assigned to correct language", keywordsPage.getText().contains("한국"));
	}

	@Test
	public void testKeywordsCreationAndDeletionOnSecondWindow() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("double duo two pair couple", "Urdu");
		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("Urdu");
		keywordsPage.loadOrFadeWait();

		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = keywordsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final KeywordsPage secondKeywordsPage = (new AppBody(getDriver())).getKeywordsPage();
		assertEquals(5, secondKeywordsPage.countSynonymLists());

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = body.getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("couple", "two");

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(4, secondKeywordsPage.countSynonymLists());

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = body.getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("pair", "duo");

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(3, secondKeywordsPage.countSynonymLists());
	}

	@Test
	public void testSynonymsNotCaseSensitive() {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
		createKeywordsPage.selectLanguage("English");
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();

		createKeywordsPage.addSynonyms("bear");
		assertEquals(1, createKeywordsPage.countKeywords());

		for (final String bearVariant : Arrays.asList("Bear", "beaR", "BEAR", "beAR", "BEar")) {
			createKeywordsPage.addSynonyms(bearVariant);
			assertEquals(1, createKeywordsPage.countKeywords());
			assertThat("bear not included as a keyword", createKeywordsPage.getProspectiveKeywordsList().contains("bear"));
			assertThat("correct error message not showing", createKeywordsPage.getText().contains(bearVariant + " is a case variant of bear, an existing keyword."));
		}

		// disallows any adding of synonyms if disallowed synonym found
		createKeywordsPage.addSynonyms("Polar Bear");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("bear not included as a keyword", createKeywordsPage.getProspectiveKeywordsList().contains("bear"));
		assertThat("correct error message not showing", createKeywordsPage.getText().contains("Bear is a case variant of bear, an existing keyword."));

		//jam and jaM are case variants so none should be added
		createKeywordsPage.addSynonyms("jam jaM");
		assertEquals(1, createKeywordsPage.countKeywords());
	}

	@Test
	public void testSpinnerPresentOnLastSynonymWhilePenultimateSynonymSpinnerPresent() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("ying yang", "Korean");
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("Korean");
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		assertEquals(2, keywordsPage.countSynonymLists());

		keywordsPage.getSynonymIcon("ying", "yang").click();
		if (keywordsPage.getSynonymIcon("ying", "yang").getAttribute("class").contains("fa-spin")) {
			assertThat("Spinner not present on last synonym", keywordsPage.getSynonymIcon("yang", "yang").getAttribute("class").contains("fa-spin"));
		}
	}

	@Test
	public void testBooleanTermsNotValidKeyword() {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
		createKeywordsPage.selectLanguage("English");
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addSynonyms("holder");
		assertEquals(1, createKeywordsPage.countKeywords());
		final List<String> booleanProximityOperators = Arrays.asList("NOT", "NEAR", "DNEAR", "SOUNDEX", "XNEAR", "YNEAR", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN");

		for (final String operator : booleanProximityOperators) {
			createKeywordsPage.addSynonyms(operator);
			assertThat("boolean operator \"" + operator + "\" should not be added as a synonym", !createKeywordsPage.getProspectiveKeywordsList().contains(operator));
			assertThat("Correct error message not showing", createKeywordsPage.getText().contains(operator + " is a boolean or proximity operator. These are invalid"));
			assertEquals(1, createKeywordsPage.countKeywords());
		}

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		createKeywordsPage.loadOrFadeWait();

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
		createKeywordsPage.selectLanguage("English");
		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
		createKeywordsPage.loadOrFadeWait();
		createKeywordsPage.addBlacklistedTerms("holder");
		assertEquals(1, createKeywordsPage.countKeywords());

		for (final String operator : booleanProximityOperators) {
			createKeywordsPage.addBlacklistedTerms(operator);
			assertThat("boolean operator \"" + operator + "\" should not be added as a synonym", !createKeywordsPage.getProspectiveKeywordsList().contains(operator));
			assertThat("Correct error message not showing", createKeywordsPage.getText().contains(operator + " is a boolean or proximity operator. These are invalid"));
			assertEquals(1, createKeywordsPage.countKeywords());
		}

		createKeywordsPage.cancelWizardButton(CreateNewKeywordsPage.WizardStep.TRIGGERS).click();
		createKeywordsPage.loadOrFadeWait();
	}

	@Test
	public void testAddKeywordsBoxOpenClickDelete() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("бір екі үш төрт бес", "Kazakh");

		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.selectLanguage("Kazakh");
		keywordsPage.synonymGroupPlusButton("бір").click();
		assertTrue(keywordsPage.synonymGroupTextBox("бір").isDisplayed());

		keywordsPage.deleteSynonym("екі", "үш");
		assertTrue(keywordsPage.synonymGroupTextBox("бір").isDisplayed());
	}

	@Test
	public void testQuickSynonymDelete() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("string strong strang streng strung", "German");
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("German");
		try {
			keywordsPage.scrollIntoViewAndClick(keywordsPage.getSynonymIcon("strong", "strung"));
			keywordsPage.scrollIntoViewAndClick(keywordsPage.getSynonymIcon("string", "strung"));
		} catch (final WebDriverException w) {
			fail("Unable to delete a synonym quickly");
		}
		Thread.sleep(5000);
		assertEquals("Incorrect number of synonyms", 3, keywordsPage.countSynonymLists());
	}

	@Test
	public void testAddingForbiddenKeywordsFromUrl() {
		String blacklistUrl = getConfig().getWebappUrl() + "/p/keywords/create/blacklisted/English/";
		String synonymsUrl = getConfig().getWebappUrl() + "/p/keywords/create/synonyms/English/";
		if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
			blacklistUrl = getConfig().getWebappUrl() + "/p/keywords/create/blacklisted/englishUTF8/";
			synonymsUrl = getConfig().getWebappUrl() + "/p/keywords/create/synonyms/englishUTF8/";
		}
		for (final String forbidden : Arrays.asList("(", "\"", "OR")) {
			getDriver().get(blacklistUrl.concat(forbidden));
			body.loadOrFadeWait();
			createKeywordsPage = body.getCreateKeywordsPage();
			assertFalse(forbidden + " is a forbidden keyword and should not be included in the prospective blacklist list", createKeywordsPage.getProspectiveKeywordsList().contains("("));

			getDriver().get(synonymsUrl.concat(forbidden));
			body.loadOrFadeWait();
			createKeywordsPage = body.getCreateKeywordsPage();
			assertFalse(forbidden + " is a forbidden keyword and should not be included in the prospective synonyms list", createKeywordsPage.getProspectiveKeywordsList().contains("("));
		}
	}

	@Test
	public void testSynonymsDisplayedInAlphabeticalOrder() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();

		for (final String synonyms : Arrays.asList("aa ba ca da", "ab bb cb db", "dc cc bc ac", "ca ba da aa")) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = body.getCreateKeywordsPage();
			createKeywordsPage.createSynonymGroup(synonyms, "English");

			searchPage = body.getSearchPage();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			final List<String> keywords = searchPage.getLeadSynonymsList();

			for (int i = 0; i < keywords.size() - 1; i++) {
				assertTrue(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0);
			}
			searchPage.loadOrFadeWait();
			new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage.loadOrFadeWait();
		keywordsPage = body.getKeywordsPage();
		keywordsPage.searchFilterTextBox().sendKeys("cc");
		final List<String> keywords = keywordsPage.getLeadSynonymsList();

		for (int i = 0; i < keywords.size() - 1; i++) {
			assertTrue(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0);
		}
	}

	@Test
	public void testBlacklistedKeywordsDisplayedInAlphabeticalOrder() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");

		keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);

		final List<String> keywords = keywordsPage.getBlacklistedTerms();
		assertEquals("Wrong number of blacklist items created", 8, keywords.size());

		for (int i = 0; i < keywords.size() - 1; i++) {
			assertTrue(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0);
		}
	}

	@Test
	public void testDeletingOfSynonymsAndBlacklistedTerms() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");
		searchPage = body.getSearchPage();
		searchPage.waitForSearchLoadIndicatorToDisappear();
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);

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

		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);

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
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = body.getKeywordsPage();

		final List<String> synonyms = Arrays.asList("ea", "es", "ed", "ef", "eg");
		for (final String synonym : synonyms) {
			keywordsPage.getSynonymIcon(synonym, synonym).click();
			assertEquals("Too many synonyms are disabled on synonym delete", 6 - synonyms.indexOf(synonym), keywordsPage.countDisabledKeywords());
			Thread.sleep(3000);
			assertFalse("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled());
		}
	}

	@Test
	public void testAllowKeywordStringsThatContainBooleansWithinThem() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = body.getCreateKeywordsPage();
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
		createKeywordsPage = body.getCreateKeywordsPage();
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
		createKeywordsPage = body.getCreateKeywordsPage();
		createKeywordsPage.createSynonymGroup("place holder", "English");
		new SideNavBar(getDriver()).switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("English");
		keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

		for (final String hiddenBooleansProximity : hiddenSearchOperators) {
			keywordsPage.addSynonymToGroup(hiddenBooleansProximity, "holder");
			keywordsPage.loadOrFadeWait();
			assertEquals(1, keywordsPage.countSynonymGroupsWithLeadSynonym(hiddenBooleansProximity.toLowerCase()));
		}
	}
}