package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.NotificationsDropDown;
import com.autonomy.abc.selenium.page.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.KeywordsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;


public class KeywordsPageAndWizardITCase extends ABCTestBase {
	public KeywordsPageAndWizardITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private KeywordsPage keywordsPage;
	private CreateNewKeywordsPage createKeywordsPage;
	private SearchPage searchPage;
	private NotificationsDropDown notifications;

	@Before
	public void setUp() throws MalformedURLException {
		keywordsPage = body.getKeywordsPage();
		createKeywordsPage = body.getCreateKeywordsPage();
		notifications = body.getNotifications();
	}

	@Test
	public void testCreateNewKeywordsButtonAndCancel() {
		assertThat("Create new keywords button is not visible", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));
		assertThat("Create new keywords button should not be visible", !keywordsPage.createNewKeywordsButton().isDisplayed());
		assertThat("Create Synonyms button should be visible", createKeywordsPage.keywordsType("SYNONYMS").isDisplayed());
		assertThat("Create Blacklisted button should be visible", createKeywordsPage.keywordsType("BLACKLISTED").isDisplayed());
		assertThat("Cancel button be visible", createKeywordsPage.cancelWizardButton("type").isDisplayed());
		assertThat("Continue button should be visible", createKeywordsPage.continueWizardButton("type").isDisplayed());

		createKeywordsPage.cancelWizardButton("type").click();
		assertThat("Create new keywords button should be visible", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateSynonymsWizard() {
		keywordsPage.createNewKeywordsButton().click();
		assertThat("Continue button should be disabled until a keywords type is selected", keywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton("type"), "disabled"));

		createKeywordsPage.keywordsType("SYNONYMS").click();
		assertThat("Synonym type not set active", createKeywordsPage.keywordsType("SYNONYMS").getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", !createKeywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		createKeywordsPage.continueWizardButton("type").click();
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().clear();
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));
		assertThat("Add synonyms button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.addSynonymsButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("horse");
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsButton().click();
		assertThat("Finish button should be disabled until more than one synonym is added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("stuff pony things");
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));
		assertEquals(4, createKeywordsPage.countKeywords());

		createKeywordsPage.finishSynonymWizardButton().click();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		final List<String> searchTerms = searchPage.getSearchTermsList();
		assertThat("Synonym group does not contain 'stuff', 'horse', 'pony' and 'things'", searchTerms.containsAll(Arrays.asList("stuff", "horse", "pony", "things")));

		navBar.getTab(NavBarTabId.KEYWORDS).click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym horse is not displayed", keywordsPage.leadSynonym("horse").isDisplayed());

		final List<String> synonymGroup = keywordsPage.getSynonymGroupSynonyms("horse");
		assertThat("Synonym group does not contain 'stuff', 'horse', 'pony' and 'things'", synonymGroup.containsAll(Arrays.asList("stuff", "horse", "pony", "things")));
	}

	@Test
	public void testWizardCancelButtonsWorksAfterClickingTheNavBarToggleButton() {
		keywordsPage.createNewKeywordsButton().click();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton("type").click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		createKeywordsPage.keywordsType("SYNONYMS").click();
		createKeywordsPage.continueWizardButton("type").click();
		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton("synonyms").click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("BLACKLISTED").click();
		createKeywordsPage.continueWizardButton("type").click();
		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton("blacklisted").click();
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateBlacklistedWizard() {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		assertThat("Continue button should be disabled until a keywords type is selected", createKeywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton("type"), "disabled"));

		createKeywordsPage.keywordsType("BLACKLISTED").click();
		assertThat("Blacklisted type not set active", createKeywordsPage.keywordsType("BLACKLISTED").getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", !createKeywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		createKeywordsPage.continueWizardButton("type").click();
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().clear();
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.addBlacklistTermsButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("danger");
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTextBox().sendKeys("warning beware scary");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));
		assertEquals(4, createKeywordsPage.countKeywords());

		createKeywordsPage.finishBlacklistWizardButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		final List<String> blacklistTerms = keywordsPage.getBlacklistedTerms();
		assertThat("blacklist terms not all displayed", blacklistTerms.containsAll(Arrays.asList("danger", "warning", "beware", "scary")));
		assertThat("too many blacklist terms", blacklistTerms.size() == 4);
	}

	@Test
	public void testKeywordsFilter() throws InterruptedException {
		final WebDriverWait wait = new WebDriverWait(getDriver(), 5);
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("dog hound canine");

		searchPage = body.getSearchPage();
		wait.until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText().contains("dog"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText().contains("hound"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText().contains("canine"));

		navBar.getTab(NavBarTabId.KEYWORDS).click();
		wait.until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog").containsAll(Arrays.asList("hound", "canine")));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound").containsAll(Arrays.asList("dog", "canine")));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine").containsAll(Arrays.asList("dog", "hound")));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createBlacklistedTerm("illegal");
		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms().contains("illegal"));

		keywordsPage.filterView("synonyms");
		assertThat("Blacklist terms are still visible", keywordsPage.getBlacklistedTerms().size() == 0);
		assertThat("A synonym list on row 2 is not visible", keywordsPage.synonymList(1).isDisplayed());
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog").containsAll(Arrays.asList("hound", "canine")));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound").containsAll(Arrays.asList("dog", "canine")));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine").containsAll(Arrays.asList("dog", "hound")));

		keywordsPage.filterView("blacklist");
		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms().contains("illegal"));
		assertThat("There should not be a a synonym list on row 2", !keywordsPage.synonymList(1).isDisplayed());

		keywordsPage.filterView(("all"));
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
		createKeywordsPage.createSynonymGroup("frog toad amphibian tadpole");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("frog").containsAll(Arrays.asList("frog", "toad", "amphibian", "tadpole")));
		assertEquals(keywordsPage.countSynonymLists(), 4);
		assertEquals(keywordsPage.getSynonymGroupSynonyms("frog").size(), 4);

		keywordsPage.deleteSynonym("amphibian", "toad");
		assertEquals(keywordsPage.countSynonymLists(), 3);
		assertEquals(keywordsPage.getSynonymGroupSynonyms("toad").size(), 3);
		assertThat("the synonym amphibian should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("tadpole").contains("amphibian"));
		assertThat("the synonym amphibian should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("toad").contains("amphibian"));
		assertThat("the synonym amphibian should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("frog").contains("amphibian"));

		keywordsPage.deleteSynonym("frog", "frog");
		assertEquals(keywordsPage.countSynonymLists(), 2);
		assertEquals(keywordsPage.getSynonymGroupSynonyms("toad").size(), 2);
		assertThat("the synonym frog should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("toad").contains("frog"));
		assertThat("the synonym frog should be deleted from every synonym list", !keywordsPage.getSynonymGroupSynonyms("tadpole").contains("frog"));

		keywordsPage.deleteSynonym("tadpole", "toad");
		assertEquals(keywordsPage.countSynonymLists(), 0);
	}

	@Test
	public void testDeleteSynonymsFromOverlappingSynonymGroups() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("wine merlot shiraz bordeaux");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("wine red scarlet burgundy");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
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

	@Test
	public void testCreateDuplicateBlacklist() {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createBlacklistedTerm("fish");
		assertThat("Blacklist fish not visible", keywordsPage.getBlacklistedTerms().contains("fish"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("BLACKLISTED").click();
		createKeywordsPage.continueWizardButton("type").click();
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("fish");
		createKeywordsPage.addBlacklistTermsButton().click();
		createKeywordsPage.loadOrFadeWait();
		assertThat("Duplicate blacklist warning message not present", createKeywordsPage.getText().contains("The word \"fish\" is already blacklisted"));
		assertThat("Duplicate blacklist term should not be added", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("chips");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Duplicate blacklist warning message has not disappeared", !createKeywordsPage.getText().contains("The word \"fish\" is already blacklisted"));
		assertThat("New blacklist term should be added", createKeywordsPage.countKeywords() == 1);
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));

		createKeywordsPage.deleteKeyword("chips");
		assertThat("There should be no blacklist terms", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.finishBlacklistWizardButton(), "disabled"));

		createKeywordsPage.cancelWizardButton("blacklisted").click();
		assertThat("Cancel button redirects to wrong page", getDriver().getCurrentUrl().endsWith("keywords"));
		assertEquals(1, keywordsPage.getBlacklistedTerms().size());
	}

	@Test
	public void testWhitespaceBlacklistTermsWizard() {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("BLACKLISTED").click();
		createKeywordsPage.continueWizardButton("type").click();
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

	@Test
	public void testWhitespaceSynonymsWizard() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("SYNONYMS").click();
		createKeywordsPage.continueWizardButton("type").click();
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

	@Test
	public void testQuotesInSynonymsWizard() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("SYNONYMS").click();
		createKeywordsPage.continueWizardButton("type").click();

		createKeywordsPage.addSynonyms("\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addSynonyms("\"\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addSynonyms("\" \"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addSynonyms("test");
		createKeywordsPage.addSynonyms("\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addSynonyms("\"\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addSynonyms("\" \"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));
	}

	@Test
	public void testQuotesInBlacklistWizard() {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("BLACKLISTED").click();
		createKeywordsPage.continueWizardButton("type").click();

		createKeywordsPage.addBlacklistedTerms("\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addBlacklistedTerms("\"\"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addBlacklistedTerms("\" \"");
		assertEquals(0, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addBlacklistedTerms("test");
		createKeywordsPage.addBlacklistedTerms("\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addBlacklistedTerms("\"\"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		createKeywordsPage.addBlacklistedTerms("\" \"");
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("Incorrect/No error message displayed", createKeywordsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));
	}

	@Ignore // This takes a long time
	@Test
	public void testAddLotsOfSynonymGroups() throws IOException, InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		final List<String> groupsOfFiveSynonyms = keywordsPage.loadTextFileLineByLineIntoList("C://dev//res//100SynonymGroups.txt");

		for (final String synonymGroup : groupsOfFiveSynonyms) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage.createSynonymGroup(synonymGroup);

			navBar.switchPage(NavBarTabId.KEYWORDS);
			assertThat("Wrong number of synonym lists", keywordsPage.countSynonymLists() == groupsOfFiveSynonyms.indexOf(synonymGroup) + 1);
		}
	}

	@Test
	public void testAddingWhitespaceAndQuotesOnKeywordsPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("one two three");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.addSynonymToGroup("four", "one");
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two").contains("four"));
		assertEquals(4, keywordsPage.countSynonymLists());

		keywordsPage.synonymGroup("three").findElement(By.cssSelector(".fa-plus")).click();
		keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).clear();
		keywordsPage.synonymGroup("three").findElement(By.cssSelector(".fa-check")).click();
		assertThat("add synonym box should still be displayed", keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).isDisplayed());

		keywordsPage.searchFilterTextBox().click();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two").contains("four"));
		assertEquals(4, keywordsPage.countSynonymLists());

		for (final String badSynonym : Arrays.asList(" ", "\t", "\"", "\" \"")) {
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".fa-plus")).click();
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).clear();
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).sendKeys(badSynonym);
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".fa-check")).click();
			assertThat("add synonym box should still be displayed", keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).isDisplayed());

			keywordsPage.searchFilterTextBox().click();
			assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("one").size() == 4);
			assertEquals(4, keywordsPage.countSynonymLists());
		}
	}

	@Test
	public void testNotificationForCreatedBlacklistedTermAndSynonymGroup() throws InterruptedException {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createBlacklistedTerm("orange");
		body.waitForGritterToClear();
		navBar.switchPage(NavBarTabId.OVERVIEW);

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("piano keyboard pianoforte");
		body.waitForGritterToClear();

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.deleteSynonym("keyboard", "piano");
		body.waitForGritterToClear();
		navBar.switchPage(NavBarTabId.PROMOTIONS);

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Updated a synonym group containing: piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText().contains("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(3).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));

		keywordsPage.deleteBlacklistedTerm("orange");
		body.waitForGritterToClear();
		navBar.switchPage(NavBarTabId.OVERVIEW);

		topNavBar.notificationsDropdown();
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText().contains("Removed \"orange\" from the blacklist"));
		assertThat("Notification text incorrect", notifications.notificationNumber(2).getText().contains("Updated a synonym group containing: piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(3).getText().contains("Created a new synonym group containing: keyboard, piano, pianoforte"));
		assertThat("Notification text incorrect", notifications.notificationNumber(4).getText().contains("Added \"orange\" to the blacklist"));

		notifications.notificationNumber(1).click();
		assertThat("notification link has not directed back to the keywords page", getDriver().getCurrentUrl().contains("keyword"));
	}

	@Test
	public void testCreateBlacklistedTermFromSearchPage() {
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("noir");
		searchPage = body.getSearchPage();
		searchPage.loadOrFadeWait();

		assertThat("No results for search noir", searchPage.docLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.blacklistLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to blacklist wizard", getDriver().getCurrentUrl().contains("keywords/create"));
		assertThat("link not directing to blacklist wizard", createKeywordsPage.getText().contains("Select terms to blacklist"));
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("noir");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

		createKeywordsPage.finishBlacklistWizardButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("Blacklisted term not added", keywordsPage.getBlacklistedTerms().contains("noir"));
	}

	@Test
	public void testCreateSynonymGroupFromSearchPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("rouge");
		searchPage = body.getSearchPage();

		assertThat("No results for search rouge", searchPage.docLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl().contains("keywords/create"));
		assertThat("link not directing to synonym group wizard", createKeywordsPage.getText().contains("Select synonyms"));
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList().contains("rouge"));
		assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("rouge");
		createKeywordsPage.addSynonymsButton().click();
		assertEquals(1, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList().contains("rouge"));
		assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("red");
		createKeywordsPage.addSynonymsButton().click();
		assertEquals(2, createKeywordsPage.countKeywords());
		assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList().contains("rouge"));
		assertThat("keywords list does not include term 'red'", createKeywordsPage.getProspectiveKeywordsList().contains("red"));
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));

		createKeywordsPage.finishSynonymWizardButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);
		assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("rouge").contains("red"));
		assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("red").contains("rouge"));
		assertEquals(2, keywordsPage.countSynonymLists());
	}

	@Test
	public void testCreateSynonymGroupFromMultiTermSearchOnSearchPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("lodge dodge podge");
		searchPage = body.getSearchPage();

		assertThat("No results for search", searchPage.docLogo().isDisplayed());
		assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
		assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl().contains("keywords/create"));
		assertThat("link not directing to synonym group wizard", createKeywordsPage.getText().contains("Select synonyms"));
		assertEquals(3, createKeywordsPage.countKeywords());
		assertThat("Wrong prospective blacklisted terms added", createKeywordsPage.getProspectiveKeywordsList().containsAll(Arrays.asList("lodge", "dodge", "podge")));
		assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishSynonymWizardButton(), "disabled"));

		createKeywordsPage.finishSynonymWizardButton().click();
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);
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
		createKeywordsPage.createSynonymGroup("grizzly brownBear bigBear");
		final List<String> synonymListBears = Arrays.asList("grizzly", "brownBear", "bigBear");
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title().contains(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor().contains(synonym));
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertEquals(3, searchPage.countSynonymLists());
		}

		navBar.switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("honeyBee bumbleBee buzzyBee");
		final List<String> synonymListBees = Arrays.asList("honeyBee", "bumbleBee", "buzzyBee");
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListBees) {
			assertThat(synonym + " not included in title", searchPage.title().contains(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor().contains(synonym));
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBees));
			assertEquals(3, searchPage.countSynonymLists());
		}

		navBar.switchPage(NavBarTabId.KEYWORDS);
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
		List<String> synonymListBears = Arrays.asList("grizzly", "brownBear", "bigBear");
		createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListBears, ' '));
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title().contains(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor().contains(synonym));
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertEquals(3, searchPage.countSynonymLists());
		}

		searchPage.addSynonymToGroup("kodiak", "grizzly");
		searchPage.loadOrFadeWait();
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym).contains("kodiak"));
			assertEquals(3, searchPage.countSynonymLists());
		}

		searchPage.deleteSynonym("bigBear", "brownBear");
		searchPage.loadOrFadeWait();
		synonymListBears = Arrays.asList("grizzly", "brownBear");
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertThat("bigBear not deleted from group " + synonym, !searchPage.getSynonymGroupSynonyms(synonym).contains("bigBear"));
			assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym).contains("kodiak"));
			assertEquals(2, searchPage.countSynonymLists());
		}

		navBar.switchPage(NavBarTabId.KEYWORDS);
		assertEquals(3, keywordsPage.countSynonymLists());

		synonymListBears = Arrays.asList("grizzly", "brownBear", "kodiak");
		for (final String synonym : synonymListBears) {
			assertThat(synonym + " group incomplete", keywordsPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListBears));
			assertEquals(3, keywordsPage.getSynonymGroupSynonyms(synonym).size());
			assertThat("bigBear not deleted from group " + synonym, !keywordsPage.getSynonymGroupSynonyms(synonym).contains("bigBear"));
		}
	}

	@Test
	public void testNoBlacklistLinkForBlacklistedSearch() {
		keywordsPage.deleteAllBlacklistedTerms();
		topNavBar.search("wizard");
		searchPage = body.getSearchPage();

		searchPage.blacklistLink().click();
		createKeywordsPage.finishBlacklistWizardButton().click();
		keywordsPage.loadOrFadeWait();
		assertThat("Blacklisted term not created", keywordsPage.getBlacklistedTerms().contains("wizard"));

		topNavBar.search("wizard");
		new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		assertThat("you searched for incorrect", searchPage.youSearchedFor().contains("wizard"));
		assertThat("Keywords incorrect", searchPage.getBlacklistedTerms().contains("wizard"));
		assertThat("link to blacklist or create synonyms should not be present", !searchPage.getText().contains("You can create synonyms or blacklist these search terms"));
	}

	@Test
	public void testSynonymGroupMembersSearchWholeGroup() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		final List<String> synonymListCars = Arrays.asList("car", "auto", "motor");
		createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListCars, ' '));
		searchPage = body.getSearchPage();

		for (final String synonym : synonymListCars) {
			topNavBar.search(synonym);
			assertEquals(1, searchPage.countSynonymLists());
			assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms(synonym).containsAll(synonymListCars));
		}
	}
}