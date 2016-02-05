package com.autonomy.abc.keywords;

import com.autonomy.abc.Trigger.SharedTriggerTests;
import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.lift.Matchers.displayed;

public class KeywordsWizardITCase extends ABCTestBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(KeywordsWizardITCase.class); 
    private KeywordsPage keywordsPage;
    private CreateNewKeywordsPage createKeywordsPage;
    private SearchPage searchPage;
    private KeywordService keywordService;
    private TriggerForm triggerForm;

    public KeywordsWizardITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        keywordService = getApplication().keywordService();

        keywordsPage = keywordService.deleteAll(KeywordFilter.ALL);

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
    }

    @After
    public void tearDown() {
        keywordService.deleteAll(KeywordFilter.ALL);
    }

    @Test
    public void testCreateNewKeywordsButtonAndCancel() {
        assertThat("Cancel button displayed", createKeywordsPage.cancelWizardButton(), is(displayed()));
        createKeywordsPage.cancelWizardButton().click();

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
        assertThat("Continue button should be disabled until a keywords type is selected", ElementUtil.isAttributePresent(createKeywordsPage.continueWizardButton(), "disabled"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
        assertThat("Synonym type not set active", ElementUtil.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM)).getAttribute("class"), containsString("progressive-disclosure-selection"));
        assertThat("Continue button should be enabled", createKeywordsPage.continueWizardButton().getAttribute("class"), not(containsString("disabled")));
        assertThat("languages select should be visible", createKeywordsPage.languagesSelectBox().isDisplayed());

        if(getConfig().getType() == ApplicationType.ON_PREM) {
            createKeywordsPage.selectLanguage(Language.FRENCH);
            assertThat(createKeywordsPage.languagesSelectBox().getText(), equalToIgnoringCase("French"));
        } else {
            LOGGER.warn("Cannot select language for synonyms yet");
        }

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();
        assertThat("Finish button should be disabled until synonyms are added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm = createKeywordsPage.getTriggerForm();

        triggerForm.clearTriggerBox();
        assertThat("Finish button should be disabled until synonyms are added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertThat("Add synonyms button should be disabled until synonyms are added", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));

        triggerForm.typeTriggerWithoutSubmit("horse");
        assertThat("Finish button should be disabled until synonyms are added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm.addButton().click();
        assertThat("Finish button should be disabled until more than one synonym is added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertEquals(1, triggerForm.getNumberOfTriggers());

        triggerForm.addTrigger("stuff pony things");
        assertThat("Finish button should be enabled", !ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertEquals(4, triggerForm.getNumberOfTriggers());

        createKeywordsPage.enabledFinishWizardButton().click();
        Waits.loadOrFadeWait();
        searchPage = getElementFactory().getSearchPage();
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

        searchPage.selectLanguage(Language.FRENCH);

        searchPage.waitForSearchLoadIndicatorToDisappear();

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            new IndexFilter("news_eng").apply(searchPage);
        }

        searchPage.waitForDocLogo();
        final List<String> searchTerms = searchPage.youSearchedFor();
        assertThat("search for 1 synonym after creating synonym group", searchTerms, hasSize(1));
        assertThat(searchTerms.get(0), isIn(Arrays.asList("stuff", "horse", "pony", "things")));

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.filterView(KeywordFilter.ALL);

        keywordsPage.selectLanguage(Language.FRENCH);
        assertThat("synonym horse is not displayed", keywordsPage.getAllKeywords(), hasItem("horse"));

        final List<String> synonymGroup = keywordsPage.getSynonymGroupSynonyms("horse");
        assertThat(synonymGroup,hasItems("stuff", "horse", "pony", "things"));
    }

    @Test
    public void testWizardCancelButtonsWorksAfterClickingTheNavBarToggleButton() {
        assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

        getElementFactory().getSideNavBar().toggle();
        createKeywordsPage.cancelWizardButton().click();
        assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();
        getElementFactory().getSideNavBar().toggle();
        createKeywordsPage.cancelWizardButton().click();
        assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("Not directed to wizard URL", getDriver().getCurrentUrl(), containsString("keywords/create"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();
        getElementFactory().getSideNavBar().toggle();
        createKeywordsPage.cancelWizardButton().click();
        assertThat("Cancel button does not work after clicking the toggle button", keywordsPage.createNewKeywordsButton().isDisplayed());
    }

    @Test
    public void testNavigateBlacklistedWizard() throws InterruptedException {
        assertThat("Continue button should be disabled until a keywords type is selected", ElementUtil.isAttributePresent(createKeywordsPage.continueWizardButton(), "disabled"));

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
        assertThat("Blacklisted type not set active", ElementUtil.getFirstChild(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST)).getAttribute("class"), containsString("progressive-disclosure-selection"));
        assertThat("Continue button should be enabled", createKeywordsPage.continueWizardButton().getAttribute("class"), not(containsString("disabled")));

        assertThat("Wizard did not navigate to languages page", createKeywordsPage.languagesSelectBox().isDisplayed());

        if(getConfig().getType() == ApplicationType.ON_PREM) {
            createKeywordsPage.selectLanguage(Language.SWAHILI);
            assertThat(createKeywordsPage.languagesSelectBox().getText(), equalToIgnoringCase("Swahili"));

            createKeywordsPage.selectLanguage(Language.ENGLISH);
            assertThat(createKeywordsPage.languagesSelectBox().getText(), equalToIgnoringCase("English"));
        } else {
            LOGGER.warn("Cannot select language for blacklists yet");
        }

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();

        triggerForm = createKeywordsPage.getTriggerForm();

        assertThat("Finish button should be disabled until blacklisted terms are added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertThat("Wizard did not navigate to blacklist page", createKeywordsPage, containsText("blacklist"));

        triggerForm.clearTriggerBox();
        assertThat("Finish button should be disabled until blacklisted terms are added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertThat("Finish button should be disabled until blacklisted terms are added", ElementUtil.isAttributePresent(triggerForm.addButton(), "disabled"));

        triggerForm.typeTriggerWithoutSubmit("danger");
        assertThat("Finish button should be disabled until blacklisted terms are added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm.addButton().click();
        assertThat("Finish button should be enabled", !ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertEquals(1, triggerForm.getNumberOfTriggers());

        triggerForm.addTrigger("warning beware scary");
        assertThat("Finish button should be enabled", !ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));
        assertEquals(4, triggerForm.getNumberOfTriggers());

        createKeywordsPage.enabledFinishWizardButton().click();

        FluentWait<WebDriver> wait = new WebDriverWait(getDriver(), 30).withMessage("creating blacklist terms");
        wait.until(GritterNotice.notificationAppears());
        wait.until(GritterNotice.notificationsDisappear());
        final List<String> blacklistTerms = keywordsPage.getBlacklistedTerms();
        Waits.loadOrFadeWait();
        assertThat(blacklistTerms, containsItems(Arrays.asList("danger", "warning", "beware", "scary")));
        assertThat(blacklistTerms, hasSize(4));
    }

    //Duplicate blacklisted terms are not allowed to be created within the same language
    @Test
    @KnownBug("CSA-1791")
    public void testCreateDuplicateBlacklist() throws InterruptedException {
        final String term = "fish";
        final String other = "chips";

        keywordService.addBlacklistTerms(Language.ENGLISH, term);
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(term));

        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();
        createKeywordsPage.selectLanguage(Language.ENGLISH);

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();
        assertThat(createKeywordsPage.finishWizardButton(), disabled());

        triggerForm = createKeywordsPage.getTriggerForm();

        triggerForm.addTrigger(term);
        verifyThat(createKeywordsPage, containsText(Errors.Keywords.DUPLICATE_BLACKLIST));
        verifyKeywordCount(0);

        try {
            createKeywordsPage.finishWizardButton().click();
            WebElement notification = new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationAppears());
            verifyThat(notification, containsText(Errors.Keywords.CREATING));
            triggerForm.removeTrigger(term);
            verifyThat(createKeywordsPage.finishWizardButton(), disabled());
        } catch (WebDriverException e) {
            LOGGER.info("cannot click finish wizard button, or timed out");
            assertThat(getDriver().getCurrentUrl(), not(endsWith("keywords")));
        }

        triggerForm.addTrigger(other);
        assertThat(createKeywordsPage, not(containsText(Errors.Keywords.DUPLICATE_BLACKLIST)));
        verifyKeywordCount(1);

        triggerForm.removeTrigger(other);
        verifyKeywordCount(0);

        createKeywordsPage.cancelWizardButton().click();
        Waits.loadOrFadeWait();
        assertThat(getDriver().getCurrentUrl(), endsWith("keywords"));
        assertThat(keywordsPage.getBlacklistedTerms(), hasSize(1));
    }

    private void verifyKeywordCount(int count) {
        verifyThat(count + " keywords ready to be added", triggerForm.getNumberOfTriggers(), is(count));
        if (count > 0) {
            verifyThat(createKeywordsPage.finishWizardButton(), not(disabled()));
        } else {
            verifyThat(createKeywordsPage.finishWizardButton(), disabled());
        }
    }

    @Test
    public void testSynonymTriggers(){
        testTriggers(CreateNewKeywordsPage.KeywordType.SYNONYM);
    }

    @Test
    public void testBlacklistTriggers(){
        testTriggers(CreateNewKeywordsPage.KeywordType.BLACKLIST);
    }

    private void testTriggers(CreateNewKeywordsPage.KeywordType type) {
        createKeywordsPage.keywordsType(type).click();
        createKeywordsPage.continueWizardButton().click();
        triggerForm = createKeywordsPage.getTriggerForm();
        testBadTriggers(type);
        for (String trigger : triggerForm.getTriggersAsStrings()) {
            triggerForm.removeTrigger(trigger);
        }
        triggerForm.addTrigger("test");
        testBadTriggers(type);
    }

    private void testBadTriggers(CreateNewKeywordsPage.KeywordType type) {
        if (type == CreateNewKeywordsPage.KeywordType.BLACKLIST) {
            SharedTriggerTests.badUnquotedTriggersTest(triggerForm);
        } else {
            SharedTriggerTests.badTriggersTest(triggerForm);
        }
    }

    @Test
    public void testBooleanTermsNotValidKeyword() throws InterruptedException {
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();

        triggerForm = createKeywordsPage.getTriggerForm();

        addPlaceholder();
        tryAddingBooleanProximityOperators();

        createKeywordsPage.cancelWizardButton().click();
        Waits.loadOrFadeWait();

        keywordsPage.createNewKeywordsButton().click();

        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST))).click();

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();

        triggerForm = createKeywordsPage.getTriggerForm();

        addPlaceholder();
        tryAddingBooleanProximityOperators();

        createKeywordsPage.cancelWizardButton().click();
        Waits.loadOrFadeWait();
    }

    private void tryAddingBooleanProximityOperators(){
        final List<String> booleanProximityOperators = Arrays.asList("NOT", "NEAR", "DNEAR", "SOUNDEX", "XNEAR", "YNEAR", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN");

        int operatorsAdded = 1;
        for (final String operator : booleanProximityOperators) {
            triggerForm.addTrigger(operator);
            assertThat("boolean operator \"" + operator + "\" should not be added as a synonym", triggerForm.getTriggersAsStrings(), not(hasItem(operator)));
            assertThat("Operator not added properly. Should be lower case.", triggerForm.getTriggersAsStrings(), hasItem(operator.toLowerCase()));
            assertThat(triggerForm.getNumberOfTriggers(), is(++operatorsAdded));
//			assertThat("Correct error message not showing", createKeywordsPage.getText(), containsString(operator + " is a boolean or proximity operator. These are invalid"));
//			assertEquals(1, createKeywordsPage.countKeywords());
        }
    }

    private void addPlaceholder() {
        triggerForm.addTrigger("holder");
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
    }

    @Test
    public void testAllowKeywordStringsThatContainBooleansWithinThem() throws InterruptedException {
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

        createKeywordsPage.selectLanguage(Language.ENGLISH);

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();

        triggerForm = createKeywordsPage.getTriggerForm();

        final List<String> hiddenSearchOperators = Arrays.asList("NOTed", "ANDREW", "ORder", "WHENCE", "SENTENCED", "SENTENCE1D", "PARAGRAPHING", "PARAGRAPH2inG", "NEARLY", "NEAR123LY", "SOUNDEXCLUSIVE", "XORING", "EORE", "DNEARLY", "WNEARING", "YNEARD", "AFTERWARDS", "BEFOREHAND", "NOTWHENERED");

        addPlaceholder();
        tryAddingHiddenSearchOperators(hiddenSearchOperators);

        createKeywordsPage.cancelWizardButton().click();
        Waits.loadOrFadeWait();
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.BLACKLIST).click();

        createKeywordsPage.selectLanguage(Language.ENGLISH);

        createKeywordsPage.continueWizardButton().click();
        Waits.loadOrFadeWait();

        triggerForm = createKeywordsPage.getTriggerForm();

        addPlaceholder();
        tryAddingHiddenSearchOperators(hiddenSearchOperators);

        createKeywordsPage.cancelWizardButton().click();
        keywordService.addSynonymGroup(Language.ENGLISH, "place holder");
        keywordService.goToKeywords();
        
        keywordsPage.selectLanguage(Language.ENGLISH);
        keywordsPage.filterView(KeywordFilter.SYNONYMS);

        for (final String hiddenBooleansProximity : hiddenSearchOperators) {
            keywordsPage.addSynonymToGroup(hiddenBooleansProximity, keywordsPage.synonymGroupContaining("holder"));

            new WebDriverWait(getDriver(),120).until(new ExpectedCondition<Boolean>() {     //This is too long but after sending lots of requests it slows down a loto
                @Override
                public Boolean apply(WebDriver webDriver) {
                    return keywordsPage.synonymGroupTextBox("holder").isEnabled();
                }
            });

            Waits.loadOrFadeWait();
            assertEquals(1, keywordsPage.countSynonymGroupsWithSynonym(hiddenBooleansProximity.toLowerCase()));
        }
    }

    private void tryAddingHiddenSearchOperators(List<String> hiddenSearchOperators) {
        for (int i = 0; i < hiddenSearchOperators.size(); i++) {
            triggerForm.addTrigger(hiddenSearchOperators.get(i));
            assertThat(triggerForm.getNumberOfTriggers(), is(2 + i));
        }
    }

    @Test
    @KnownBug("CSA-1712")
    public void testCursorDoesNotMoveToEndOfText(){
        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
        createKeywordsPage.continueWizardButton().click();

        triggerForm = createKeywordsPage.getTriggerForm();

        triggerForm.typeTriggerWithoutSubmit("TESTING");

        triggerForm.typeTriggerWithoutSubmit(Keys.ARROW_LEFT, Keys.ARROW_LEFT, Keys.BACK_SPACE, Keys.BACK_SPACE);

        verifyThat(triggerForm.getTextInTriggerBox(), is("TESNG"));
        verifyThat(triggerForm.getTextInTriggerBox(), is(not("TESTN")));
    }

    @Test
    @KnownBug("CSA-1812")
    public void testExistingSynonymsShowInWizard(){
        String[] existingSynonyms = {"pentimento", "mayday", "parade"};
        String duplicate = existingSynonyms[0];
        String unrelated = "unrelated";

        keywordService.addSynonymGroup(existingSynonyms);
        keywordsPage = keywordService.goToKeywords();
        goToSynonymWizardPage();

        triggerForm = createKeywordsPage.getTriggerForm();
        triggerForm.addTrigger(duplicate);
        verifyExistingGroups(duplicate, 1);

        triggerForm.addTrigger(unrelated);
        createKeywordsPage.finishWizardButton().click();

        getElementFactory().getSearchPage();
        keywordsPage = keywordService.goToKeywords();
        goToSynonymWizardPage();

        triggerForm.addTrigger(duplicate);
        verifyExistingGroups(duplicate, 2);

        triggerForm.addTrigger(unrelated);
        verifyExistingGroups(duplicate, 2);

        triggerForm.removeTrigger(duplicate);
        verifyExistingGroups(unrelated, 1);
    }

    private void goToSynonymWizardPage(){
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

        createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();
        createKeywordsPage.continueWizardButton().click();

        triggerForm = createKeywordsPage.getTriggerForm();
    }

    private void verifyExistingGroups(String existingSynonym, int size){
        List<List<String>> existingGroups = createKeywordsPage.getExistingSynonymGroups();

        verifyThat(existingGroups.size(), is(size));
        for(List<String> group : existingGroups){
            verifyThat(group, hasItem(existingSynonym));
        }
    }
}
