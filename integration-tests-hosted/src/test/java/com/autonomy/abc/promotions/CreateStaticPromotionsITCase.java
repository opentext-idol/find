package com.autonomy.abc.promotions;

import com.autonomy.abc.base.SOTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.shared.SharedTriggerTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;

public class CreateStaticPromotionsITCase extends HostedTestBase {
    private HSODCreateNewPromotionsPage createPromotionsPage;
    private HSODPromotionService promotionService;
    private Wizard wizard;

    public CreateStaticPromotionsITCase(TestConfig config) {
        super(config);
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
        promotionService = getApplication().promotionService();

        promotionService.goToPromotions().staticPromotionButton().click();
        createPromotionsPage = getElementFactory().getCreateNewPromotionsPage();
    }

    @After
    public void tearDown() {
        SOTearDown.PROMOTIONS.tearDown(this);
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

    @Test
    public void testInvalidTriggers() {
        goToTriggerStep();
        SharedTriggerTests.badTriggersTest(createPromotionsPage.getTriggerForm());
    }

    @Test
    public void testAddRemoveTriggers() {
        goToTriggerStep();

        TriggerForm triggerForm = createPromotionsPage.getTriggerForm();

        assertThat(createPromotionsPage.getCurrentStepTitle(), is(SearchTriggerStep.TITLE));
        verifyThat(triggerForm.getTriggers(), empty());

        SharedTriggerTests.addRemoveTriggers(triggerForm, createPromotionsPage.cancelButton(), createPromotionsPage.finishButton());

        final List<String> wizardTriggers = triggerForm.getTriggersAsStrings();

        // finish wizard, wait
        wizard.next();
        getElementFactory().getSearchPage();

        PromotionsDetailPage promotionsDetailPage = promotionService.goToDetails(wizardTriggers.get(0));
        assertThat("loaded details page", promotionsDetailPage.promotionTitle().getValue(), containsString(wizardTriggers.get(0)));

        List<String> createdTriggers = promotionsDetailPage.getTriggerForm().getTriggersAsStrings();
        verifyThat(createdTriggers, hasSize(wizardTriggers.size()));
        verifyThat(createdTriggers, everyItem(isIn(wizardTriggers)));
    }
}
