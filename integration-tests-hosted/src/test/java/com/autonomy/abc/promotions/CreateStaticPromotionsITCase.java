package com.autonomy.abc.promotions;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.PromotionsDetailTriggerForm;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PromotionService;
import com.autonomy.abc.selenium.promotions.SearchTriggerStep;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.util.Errors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

public class CreateStaticPromotionsITCase extends HostedTestBase {
    private HSOPromotionsPage promotionsPage;
    private HSOCreateNewPromotionsPage createPromotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private PromotionService promotionService;
    private Wizard wizard;
    private TriggerForm triggerForm;

    public CreateStaticPromotionsITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        assumeThat(config.getType(), is(ApplicationType.HOSTED));
    }

    public void goToTriggerStep() {
        StaticPromotion promotion = new StaticPromotion("dog", "woof woof", "");
        wizard = promotion.makeWizard(createPromotionsPage);
        wizard.getCurrentStep().apply();
        wizard.next();
        wizard.getCurrentStep().apply();
        wizard.next();
    }

    @Before
    public void setUp() {
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionsPage.staticPromotionButton().click();
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
        promotionService = getApplication().createPromotionService(getElementFactory());
    }

    @After
    public void tearDown() {
        promotionService.deleteAll();
    }

    @Test
    public void testAddStaticPromotion() {
        final String title = "static promotion";
        final String content = "This is the body of my static promotion.";
        final String trigger = "horse";
        Wizard wizard = new StaticPromotion(title, content, trigger).makeWizard(createPromotionsPage);

        WebElement continueButton = createPromotionsPage.continueButton();
        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        verifyThat(continueButton, disabled());
        createPromotionsPage.documentTitle().setValue("removed");
        verifyThat(continueButton, disabled());
        createPromotionsPage.documentContent().setValue(content);
        verifyThat(continueButton, not(disabled()));
        // cannot just clear as this does not trigger correct JS event
        createPromotionsPage.documentTitle().setValue("a" + Keys.BACK_SPACE);
        verifyThat(continueButton, disabled());
        createPromotionsPage.documentTitle().setValue(title);
        wizard.next();

        // TODO: languages once enabled
        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        verifyThat(createPromotionsPage.continueButton(), not(disabled()));
        wizard.getCurrentStep().apply();
        wizard.next();

        verifyThat(createPromotionsPage.getCurrentStepTitle(), is(wizard.getCurrentStep().getTitle()));
        verifyThat(createPromotionsPage.finishButton(), disabled());
        wizard.getCurrentStep().apply();
        verifyThat(createPromotionsPage.finishButton(), not(disabled()));
        wizard.next();

        getElementFactory().getSearchPage();
    }

    private void checkBadTriggers(String[] triggers, String errorSubstring) {
        for (String trigger : triggers) {
            triggerForm.addTrigger(trigger);
            verifyThat("trigger '" + trigger + "' not added", triggerForm.getNumberOfTriggers(), is(1));
            verifyThat(triggerForm.getTriggerError(), containsString(errorSubstring));
            verifyThat(triggerForm.addButton(), disabled());
        }
    }

    @Test
    public void testInvalidTriggers() {
        final String goodTrigger = "dog";
        final String[] duplicateTriggers = {
                "dog",
                " dog",
                "dog ",
                " dog  ",
                "\"dog\""
        };
        final String[] quoteTriggers = {
                "\"bad",
                "bad\"",
                "b\"ad",
                "\"trigger with\" 3 quo\"tes"
        };
        final String[] commaTriggers = {
                "comma,",
                ",comma",
                "com,ma",
                ",,,,,,"
        };
        final String[] caseTriggers = {
                "Dog",
                "doG",
                "DOG"
        };

        goToTriggerStep();

        triggerForm = createPromotionsPage.getTriggerForm();

        assertThat(createPromotionsPage.getCurrentStepTitle(), is(SearchTriggerStep.TITLE));
        verifyThat(triggerForm.getTriggers(), empty());
        triggerForm.addTrigger(goodTrigger);
        assertThat(triggerForm.getNumberOfTriggers(), is(1));

        checkBadTriggers(duplicateTriggers, Errors.Term.DUPLICATE_EXISTING);
        checkBadTriggers(quoteTriggers, Errors.Term.QUOTES);
        checkBadTriggers(commaTriggers, Errors.Term.COMMAS);
        checkBadTriggers(caseTriggers, Errors.Term.CASE);

        triggerForm.typeTriggerWithoutSubmit("a");
        verifyThat("error message is cleared", triggerForm.getTriggerError(), isEmptyOrNullString());
        verifyThat(triggerForm.addButton(), not(disabled()));

        triggerForm.typeTriggerWithoutSubmit("    ");
        verifyThat("cannot add '     '", triggerForm.addButton(), disabled());
        triggerForm.typeTriggerWithoutSubmit("\t");
        verifyThat("cannot add '\\t'", triggerForm.addButton(), disabled());
        triggerForm.addTrigger("\"valid trigger\"");
        verifyThat("can add valid trigger", triggerForm.getNumberOfTriggers(), is(2));
    }

    @Test
    public void testAddRemoveTriggers() {
        goToTriggerStep();

        triggerForm = createPromotionsPage.getTriggerForm();

        assertThat(createPromotionsPage.getCurrentStepTitle(), is(SearchTriggerStep.TITLE));
        verifyThat(triggerForm.getTriggers(), empty());
        triggerForm.addTrigger("alpha");
        triggerForm.addTrigger("beta gamma delta");
        triggerForm.removeTrigger("gamma");
        triggerForm.removeTrigger("alpha");
        triggerForm.addTrigger("epsilon");
        triggerForm.removeTrigger("beta");
        verifyThat(triggerForm.getNumberOfTriggers(), is(2));

        // finish wizard, wait
        wizard.next();
        getElementFactory().getSearchPage();

        promotionsDetailPage = promotionService.goToDetails("delta");
        assertThat("loaded details page", promotionsDetailPage.promotionTitle().getValue(), containsString("delta"));

        List<String> triggers = triggerForm.getTriggersAsStrings();
        verifyThat(triggers, hasSize(2));
        verifyThat(triggers, hasItems("delta", "epsilon"));
    }
}
