package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;

public class KeywordsWizardITCase extends ABCTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeywordsWizardITCase.class); 
    private KeywordsPage keywordsPage;
    private CreateNewKeywordsPage createKeywordsPage;
    private SearchPage searchPage;
    private KeywordService keywordService;

    public KeywordsWizardITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp() {
        keywordService = new KeywordService(getApplication(), getElementFactory());

        keywordsPage = keywordService.deleteAll(KeywordFilter.ALL);
    }

    @After
    public void tearDown() {
        keywordService.deleteAll(KeywordFilter.ALL);
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
        assertThat("Cancel button be visible", createKeywordsPage.cancelWizardButton().isDisplayed());
        assertThat("Continue button should be visible", createKeywordsPage.continueWizardButton().isDisplayed());

        createKeywordsPage.cancelWizardButton().click();
        assertThat("Create new keywords button should be visible", keywordsPage.createNewKeywordsButton().isDisplayed());
    }


    @Test
    public void testNavigateSynonymsWizard() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("Continue button should be disabled until a keywords type is selected", keywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton(), "disabled"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
        assertThat("Synonym type not set active", createKeywordsPage.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM)).getAttribute("class"), containsString("progressive-disclosure-selection"));
        assertThat("Continue button should be enabled", createKeywordsPage.continueWizardButton().getAttribute("class"), not(containsString("disabled")));
        assertThat("languages select should be visible", createKeywordsPage.languagesSelectBox().isDisplayed());

        if(getConfig().getType() == ApplicationType.ON_PREM) {
            createKeywordsPage.selectLanguage("French");
            assertThat(createKeywordsPage.languagesSelectBox().getText(), equalToIgnoringCase("French"));
        } else {
            LOGGER.warn("Cannot select language for synonyms yet");
        }

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

//		createKeywordsPage.continueWizardButton(CreateNewKeywordsPage.WizardStep.TYPE).click();
//		createKeywordsPage.loadOrFadeWait();

        createKeywordsPage.synonymAddTextBox().clear();
        assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertThat("Add synonyms button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.synonymAddButton(), "disabled"));

        createKeywordsPage.synonymAddTextBox().sendKeys("horse");
        assertThat("Finish button should be disabled until synonyms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.synonymAddButton().click();
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
        keywordsPage.filterView(KeywordFilter.ALL);

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

        body.getSideNavBar().toggle();
        createKeywordsPage.cancelWizardButton().click();
        assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        body.getSideNavBar().toggle();
        createKeywordsPage.cancelWizardButton().click();
        assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        body.getSideNavBar().toggle();
        createKeywordsPage.cancelWizardButton().click();
        assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());
    }

    @Test
    public void testNavigateBlacklistedWizard() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("Continue button should be disabled until a keywords type is selected", createKeywordsPage.isAttributePresent(createKeywordsPage.continueWizardButton(), "disabled"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
        assertThat("Blacklisted type not set active", createKeywordsPage.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST)).getAttribute("class"), containsString("progressive-disclosure-selection"));
        assertThat("Continue button should be enabled", createKeywordsPage.continueWizardButton().getAttribute("class"), not(containsString("disabled")));

        assertThat("Wizard did not navigate to languages page", createKeywordsPage.languagesSelectBox().isDisplayed());

        if(getConfig().getType() == ApplicationType.ON_PREM) {
            createKeywordsPage.selectLanguage("Swahili");
            assertThat(createKeywordsPage.languagesSelectBox().getText(), equalToIgnoringCase("Swahili"));

            createKeywordsPage.selectLanguage("English");
            assertThat(createKeywordsPage.languagesSelectBox().getText(), equalToIgnoringCase("English"));
        } else {
            LOGGER.warn("Cannot select language for blacklists yet");
        }

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();

        assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertThat("Wizard did not navigate to blacklist page", createKeywordsPage.blacklistAddTextBox().isDisplayed());

        createKeywordsPage.blacklistAddTextBox().clear();
        assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

        createKeywordsPage.blacklistAddTextBox().sendKeys("danger");
        assertThat("Finish button should be disabled until blacklisted terms are added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.blacklistAddButton().click();
        assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertEquals(1, createKeywordsPage.countKeywords());

        createKeywordsPage.blacklistAddTextBox().sendKeys("warning beware scary");
        createKeywordsPage.blacklistAddButton().click();
        createKeywordsPage.loadOrFadeWait();
        assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertEquals(4, createKeywordsPage.countKeywords());

        createKeywordsPage.enabledFinishWizardButton().click();

        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
        Thread.sleep(5000); //Ensure all blacklist terms have shown up
        final List<String> blacklistTerms = keywordsPage.getBlacklistedTerms();
        createKeywordsPage.loadOrFadeWait();
        assertThat(blacklistTerms, hasItems("danger", "warning", "beware", "scary"));
        assertEquals("too many blacklist terms", 4, blacklistTerms.size());
    }


    //Duplicate blacklisted terms are not allowed to be created within the same language
    //CSA-1791
    @Test
    public void testCreateDuplicateBlacklist() throws InterruptedException {
        final String term = "fish";
        final String other = "chips";

        keywordService.addBlacklistTerms(Language.ENGLISH, term);
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem("fish"));

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
        createKeywordsPage.selectLanguage(Language.ENGLISH);

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        assertThat(createKeywordsPage.finishWizardButton(), disabled());

        createKeywordsPage.keywordAddInput().setAndSubmit(term);
        verifyThat(createKeywordsPage, containsText(Errors.Keywords.DUPLICATE_BLACKLIST));
        verifyKeywordCount(0);

        try {
            createKeywordsPage.finishWizardButton().click();
            WebElement notification = new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationAppears());
            verifyThat(notification, containsText(Errors.Keywords.CREATING));
            createKeywordsPage.deleteKeyword(term);
            verifyThat(createKeywordsPage.finishWizardButton(), disabled());
        } catch (WebDriverException e) {
            LOGGER.info("cannot click finish wizard button, or timed out");
            assertThat(getDriver().getCurrentUrl(), not(endsWith("keywords")));
        }

        createKeywordsPage.keywordAddInput().setAndSubmit(other);
        assertThat(createKeywordsPage, not(containsText(Errors.Keywords.DUPLICATE_BLACKLIST)));
        verifyKeywordCount(1);

        createKeywordsPage.deleteKeyword(other);
        verifyKeywordCount(0);

        createKeywordsPage.cancelWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        assertThat(getDriver().getCurrentUrl(), endsWith("keywords"));
        assertThat(keywordsPage.getBlacklistedTerms(), hasSize(1));
    }

    private void verifyKeywordCount(int count) {
        verifyThat(count + " keywords ready to be added", createKeywordsPage.countKeywords(), is(count));
        if (count > 0) {
            verifyThat(createKeywordsPage.finishWizardButton(), not(disabled()));
        } else {
            verifyThat(createKeywordsPage.finishWizardButton(), disabled());
        }
    }

    //Whitespace of any form should not be added as a blacklisted term
    @Test
    public void testWhitespaceBlacklistTermsWizard() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

        createKeywordsPage.selectLanguage("English");

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        createKeywordsPage.blacklistAddTextBox().sendKeys(" ");
        createKeywordsPage.tryClickThenTryParentClick(createKeywordsPage.blacklistAddButton());
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

        createKeywordsPage.blacklistAddTextBox().clear();
        createKeywordsPage.blacklistAddTextBox().click();
        createKeywordsPage.blacklistAddTextBox().sendKeys(Keys.RETURN);
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

        createKeywordsPage.blacklistAddTextBox().sendKeys("\t");
        createKeywordsPage.tryClickThenTryParentClick(createKeywordsPage.blacklistAddButton());
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);
    }

    //Whitespace of any form should not be added as a synonym keyword
    @Test
    public void testWhitespaceSynonymsWizard() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

        createKeywordsPage.selectLanguage("English");

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        createKeywordsPage.addSynonyms(" ");
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

        createKeywordsPage.synonymAddTextBox().clear();
        createKeywordsPage.synonymAddTextBox().click();
        createKeywordsPage.synonymAddTextBox().sendKeys(Keys.RETURN);
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

        createKeywordsPage.addSynonyms("\t");
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 0);

        createKeywordsPage.addSynonyms("test");
        createKeywordsPage.addSynonyms(" ");
        assertThat("Whitespace should not be added as a blacklist term", createKeywordsPage.countKeywords() == 1);

        createKeywordsPage.synonymAddTextBox().clear();
        createKeywordsPage.synonymAddTextBox().click();
        createKeywordsPage.synonymAddTextBox().sendKeys(Keys.RETURN);
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

        createKeywordsPage.continueWizardButton().click();
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
        LOGGER.warn("Cannot select language for blacklists yet");

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();

        createKeywordsPage.addBlacklistedTerms("\"");
        assertEquals(0, createKeywordsPage.countKeywords());
        assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

        createKeywordsPage.addBlacklistedTerms("\"\"");
        assertEquals(0, createKeywordsPage.countKeywords());
        assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

        createKeywordsPage.addBlacklistedTerms("\" \"");
        assertEquals(0, createKeywordsPage.countKeywords());
        assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

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
        assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

        createKeywordsPage.addBlacklistedTerms("\"\"");
        assertEquals(1, createKeywordsPage.countKeywords());
        assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

        createKeywordsPage.addBlacklistedTerms("\" \"");
        assertEquals(1, createKeywordsPage.countKeywords());
        assertThat("plus button should be disabled", createKeywordsPage.isAttributePresent(createKeywordsPage.blacklistAddButton(), "disabled"));

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

    @Test
    public void testBooleanTermsNotValidKeyword() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

        //createKeywordsPage.selectLanguage("English");
        LOGGER.warn("Cannot select language for blacklists yet");

        createKeywordsPage.continueWizardButton().click();
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

        createKeywordsPage.cancelWizardButton().click();
        createKeywordsPage.loadOrFadeWait();

        keywordsPage.createNewKeywordsButton().click();

        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST))).click();

        //createKeywordsPage.selectLanguage("English");
        LOGGER.warn("Cannot select language for blacklists yet");

        createKeywordsPage.continueWizardButton().click();
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

        createKeywordsPage.cancelWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
    }

    @Test
    public void testAllowKeywordStringsThatContainBooleansWithinThem() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

        createKeywordsPage.selectLanguage("English");

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        createKeywordsPage.addSynonyms("placeholder");
        assertEquals(1, createKeywordsPage.getProspectiveKeywordsList().size());

        final List<String> hiddenSearchOperators = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "NEARLY", "NEAR123LY", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");

        for (int i = 0; i < hiddenSearchOperators.size(); i++) {
            createKeywordsPage.synonymAddTextBox().clear();
            createKeywordsPage.synonymAddTextBox().sendKeys(hiddenSearchOperators.get(i));
            createKeywordsPage.synonymAddButton().click();
            createKeywordsPage.loadOrFadeWait();
            assertEquals(2 + i, createKeywordsPage.getProspectiveKeywordsList().size());
        }
        createKeywordsPage.cancelWizardButton().click();
        keywordsPage.loadOrFadeWait();
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

        createKeywordsPage.selectLanguage("English");

        createKeywordsPage.continueWizardButton().click();
        createKeywordsPage.loadOrFadeWait();
        createKeywordsPage.addBlacklistedTerms("placeholder");
        assertEquals(1, createKeywordsPage.getProspectiveKeywordsList().size());

        for (int i = 0; i < hiddenSearchOperators.size(); i++) {
            createKeywordsPage.blacklistAddTextBox().clear();
            createKeywordsPage.blacklistAddTextBox().sendKeys(hiddenSearchOperators.get(i));
            createKeywordsPage.blacklistAddButton().click();
            createKeywordsPage.loadOrFadeWait();
            assertEquals(2 + i, createKeywordsPage.getProspectiveKeywordsList().size());
        }

        createKeywordsPage.cancelWizardButton().click();
        keywordsPage.loadOrFadeWait();
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.createSynonymGroup("place holder", "English");
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage.loadOrFadeWait();

        keywordsPage.selectLanguage("English");

        keywordsPage.filterView(KeywordFilter.SYNONYMS);

        for (final String hiddenBooleansProximity : hiddenSearchOperators) {
            LOGGER.info("Adding '"+hiddenBooleansProximity+"'");

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


}
