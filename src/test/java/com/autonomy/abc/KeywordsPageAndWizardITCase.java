package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.page.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.KeywordsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

	@Before
	public void setUp() throws MalformedURLException {
		keywordsPage = body.getKeywordsPage();
		createKeywordsPage = body.getCreateKeywordsPage();
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
		assertThat("Continue button should be disabled until a keywords type is selected", createKeywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		createKeywordsPage.keywordsType("SYNONYMS").click();
		assertThat("Synonym type not set active", createKeywordsPage.keywordsType("SYNONYMS").getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", !createKeywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		createKeywordsPage.continueWizardButton("type").click();
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.finishSynonymWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addSynonymsTextBox().clear();
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.finishSynonymWizardButton().getAttribute("class").contains("disabled"));
		assertThat("Add synonyms button should be disabled until synonyms are added", createKeywordsPage.addSynonymsButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addSynonymsTextBox().sendKeys("horse");
		assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.finishSynonymWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addSynonymsButton().click();
		assertThat("Finish button should be disabled until more than one synonym is added", createKeywordsPage.finishSynonymWizardButton().getAttribute("class").contains("disabled"));
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("stuff more things");
		assertThat("Finish button should be enabled", !createKeywordsPage.finishSynonymWizardButton().getAttribute("class").contains("disabled"));
		assertEquals(4, createKeywordsPage.countKeywords());

		createKeywordsPage.finishSynonymWizardButton().click();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		final List<String> searchTerms = searchPage.getSearchTermsList();
		assertThat("Synonym group does not contain 'stuff', 'horse', 'more' and 'things'", searchTerms.containsAll(Arrays.asList("stuff", "horse", "more", "things")));

		navBar.getTab(NavBarTabId.KEYWORDS).click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym horse is not displayed", keywordsPage.leadSynonym("horse").isDisplayed());

		final List<String> synonymGroup = keywordsPage.getSynonymGroupSynonyms("horse");
		assertThat("Synonym group does not contain 'stuff', 'horse', 'more' and 'things'", synonymGroup.containsAll(Arrays.asList("stuff", "horse", "more", "things")));
	}

	@Test
	public void testWizardCancelButtonsWorksAfterClickingTheNavBarToggleButton() {
		keywordsPage.createNewKeywordsButton().click();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton("type");
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		assertThat("Not directed to wizard URL", getDriver().getCurrentUrl().contains("keywords/create"));

		createKeywordsPage.keywordsType("SYNONYMS").click();
		createKeywordsPage.continueWizardButton("type").click();
		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton("synonyms");
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("BLACKLISTED").click();
		createKeywordsPage.continueWizardButton("type").click();
		topNavBar.sideBarToggle();
		createKeywordsPage.cancelWizardButton("blacklisted");
		assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());
	}

	@Test
	public void testNavigateBlacklistedWizard() {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		assertThat("Continue button should be disabled until a keywords type is selected", createKeywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		createKeywordsPage.keywordsType("BLACKLISTED").click();
		assertThat("Blacklisted type not set active", createKeywordsPage.keywordsType("BLACKLISTED").getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Continue button should be enabled", !createKeywordsPage.continueWizardButton("type").getAttribute("class").contains("disabled"));

		createKeywordsPage.continueWizardButton("type").click();
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addBlacklistedTextBox().clear();
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));
		assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.addBlacklistTermsButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("danger");
		assertThat("Finish button should be disabled until a blacklist term is added", createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Finish button should be enabled", !createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTextBox().sendKeys("warning beware scary");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Finish button should be enabled", !createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));
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
		wait.until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
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
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
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
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("wine red scarlet burgundy");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
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
		assertThat("Finish button should be disabled", createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("fish");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Duplicate blacklist warning message not present", createKeywordsPage.getText().contains("The following keywords were not added since they already exist in the blacklist: fish"));
		assertThat("Duplicate blacklist term should not be added", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.addBlacklistedTextBox().sendKeys("chips");
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Duplicate blacklist warning message has not disappeared", !createKeywordsPage.getText().contains("The following keywords were not added since they already exist in the blacklist: fish"));
		assertThat("New blacklist term should be added", createKeywordsPage.countKeywords() == 1);
		assertThat("Finish button should be enabled", !createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));

		createKeywordsPage.deleteKeyword("chips");
		assertThat("There should be no blacklist terms", createKeywordsPage.countKeywords() == 0);
		assertThat("Finish button should be disabled", createKeywordsPage.finishBlacklistWizardButton().getAttribute("class").contains("disabled"));

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
		createKeywordsPage.addBlacklistTermsButton().click();
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addBlacklistedTextBox().clear();
		createKeywordsPage.addBlacklistedTextBox().click();
		createKeywordsPage.addBlacklistedTextBox().sendKeys(Keys.RETURN);
		assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

		createKeywordsPage.addBlacklistedTextBox().sendKeys("\t");
		createKeywordsPage.addBlacklistTermsButton().click();
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
	public void testQuotesAndBracketsInSynonymsWizard() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("SYNONYMS").click();
		createKeywordsPage.continueWizardButton("type").click();

		createKeywordsPage.addSynonyms("\"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\"\"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\" \"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("(");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms(")");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("test");
		createKeywordsPage.addSynonyms("\"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\"\"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("\" \"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms("(");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addSynonyms(")");
		assertEquals(1, createKeywordsPage.countKeywords());
	}

	@Test
	public void testQuotesAndBracketsInBlacklistWizard() {
		keywordsPage.deleteAllBlacklistedTerms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.keywordsType("BLACKLISTED").click();
		createKeywordsPage.continueWizardButton("type").click();

		createKeywordsPage.addBlacklistedTerms("\"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("\"\"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("\" \"");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("(");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms(")");
		assertEquals(0, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("test");
		createKeywordsPage.addBlacklistedTerms("\"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("\"\"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("\" \"");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms("(");
		assertEquals(1, createKeywordsPage.countKeywords());

		createKeywordsPage.addBlacklistedTerms(")");
		assertEquals(1, createKeywordsPage.countKeywords());
	}

	@Test
	public void testAddingWhitespaceQuotesAndBracketsOnKeywordsPage() throws InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage.createSynonymGroup("one two three");
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.switchPage(NavBarTabId.KEYWORDS);
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

		for (final String badSynonym : Arrays.asList("", " ", "\t", "\"", "\" \"", "(", ")")) {
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".fa-plus")).click();
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).clear();
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).sendKeys(badSynonym);
			keywordsPage.synonymGroup("three").findElement(By.cssSelector(".fa-check")).click();
			assertThat("add synonym box should still be displayed", keywordsPage.synonymGroup("three").findElement(By.cssSelector(".add-synonym-input")).isDisplayed());

			keywordsPage.searchFilterTextBox().click();
			assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two").contains("four"));
			assertEquals(4, keywordsPage.countSynonymLists());
		}
	}

}