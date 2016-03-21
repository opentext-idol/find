package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.error.Errors;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.Serializable;

import static com.autonomy.abc.framework.state.TestStateAssert.assertThat;
import static com.autonomy.abc.framework.state.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;

public class SharedTriggerTests {
    private int numberOfTriggers;
    private boolean noQuotesFlag = false;
    private final TriggerForm triggerForm;

    private final String[] duplicateTriggers = {
            "dog",
            " dog",
            "dog ",
            " dog  ",
            "\"dog\"",
            "dog house"
    };
    private final String[] quoteTriggers = {
            "\"bad",
            "bad\"",
            "b\"ad",
            "\"trigger with\" 3 quo\"tes"
    };
    private final String[] commaTriggers = {
            "comma,",
            ",comma",
            "com,ma",
            ",,,,,,",
            "one, two",
            "one , two"
    };
    private final String[] caseTriggers = {
            "Dog",
            "doG",
            "DOG"
    };
    private final String[] whitespaceTriggers = {
            " ",
            "\t"
    };

    private SharedTriggerTests(TriggerForm triggerForm){
        this.triggerForm = triggerForm;
        this.numberOfTriggers = triggerForm.getNumberOfTriggers();
    }

    /* TESTS */
    /**
     * for trigger forms that do not accept multi-word triggers (e.g. blacklist) use badUnquoteTriggersTest instead
     */
    public static int badTriggersTest(TriggerForm triggerForm){
        SharedTriggerTests sharedTriggerTests = new SharedTriggerTests(triggerForm);

        sharedTriggerTests.initialiseTriggers();
        sharedTriggerTests.checkBadTriggers();
        sharedTriggerTests.addFinalTrigger("\"Valid Trigger\"");
        return sharedTriggerTests.numberOfTriggers;
    }

    public static int badUnquotedTriggersTest(TriggerForm triggerForm) {
        SharedTriggerTests sharedTriggerTests = new SharedTriggerTests(triggerForm);
        sharedTriggerTests.noQuotesFlag = true;

        sharedTriggerTests.initialiseTriggers();
        sharedTriggerTests.checkBadTriggers();
        sharedTriggerTests.addFinalTrigger("valid");
        return sharedTriggerTests.numberOfTriggers;
    }

    public static void addRemoveTriggers (TriggerForm triggerForm, WebElement cancelButton, WebElement finishButton) {
        assertThat(triggerForm.addButton(), disabled());
        assertThat(finishButton, disabled());
        assertThat(cancelButton, not(disabled()));

        triggerForm.addTrigger("animal");
        assertThat(finishButton, not(disabled()));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("animal"));

        triggerForm.removeTrigger("animal");
        assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("animal")));
        assertThat(finishButton, disabled());

        addBushyTail(triggerForm);
    }

    public static void addRemoveTriggers(TriggerForm triggerForm){
        assertThat(triggerForm.addButton(), disabled());

        triggerForm.addTrigger("animal");
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("animal"));

        triggerForm.removeTrigger("animal");
        assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("animal")));

        addBushyTail(triggerForm);
    }

    /* Helper methods */
    private void initialiseTriggers() {
        String initialTrigger = "dog";
        if(!triggerForm.getTriggersAsStrings().contains(initialTrigger)){
            triggerForm.addTrigger(initialTrigger);
            numberOfTriggers++;
        }

        assertThat(triggerForm.getNumberOfTriggers(), is(numberOfTriggers));
    }

    private void checkBadTriggers(){
        checkBadTriggers(duplicateTriggers, Errors.Term.DUPLICATE_EXISTING);
        checkBadTriggers(quoteTriggers, Errors.Term.QUOTES);
        checkBadTriggers(commaTriggers, Errors.Term.COMMAS);
        checkBadTriggers(caseTriggers, Errors.Term.CASE);
        checkBadTriggers(new String[]{"jam JAm"}, Errors.Term.DUPLICATED);
        checkWhitespaceTriggers();
        checkHTMLTrigger();
    }

    private void checkHTMLTrigger(){
        final String searchTrigger = "<h1>Hey</h1>";
        triggerForm.addTrigger(searchTrigger);

        assertThat("HTML was escaped", triggerForm.getTriggerStringOnPage(searchTrigger), is(searchTrigger.toLowerCase()));

        triggerForm.removeTrigger(searchTrigger);
    }

    private void checkWhitespaceTriggers(){
        for(String trigger : whitespaceTriggers){
            triggerForm.addTrigger(trigger);
            verifyTriggerNotAdded(trigger, Errors.Term.BLANK);
        }

        triggerForm.clearTriggerBox();
        triggerForm.typeTriggerWithoutSubmit(Keys.RETURN);
        verifyThat(triggerForm.addButton(), is(disabled()));
    }

    private void checkBadTriggers(String[] triggers, Serializable errorSubstring) {
        for (String trigger : triggers) {
            triggerForm.addTrigger(trigger);
            if (noQuotesFlag && trigger.contains("\"")) {
                verifyTriggerNotAdded(trigger, Errors.Term.NO_QUOTES);
            } else {
                verifyTriggerNotAdded(trigger, errorSubstring);
            }
        }
    }

    private void verifyTriggerNotAdded(String trigger, Serializable errorSubstring){
        verifyNoNewTriggers(trigger);
        verifyErrorString(errorSubstring);
        verifyAddButtonDisabled();
    }

    private void verifyNoNewTriggers(String trigger){
        verifyThat("trigger '" + trigger + "' not added", triggerForm.getNumberOfTriggers(), is(numberOfTriggers));
    }

    private void verifyErrorString(Serializable errorSubstring){
        verifyThat(triggerForm.getTriggerError(), containsString(errorSubstring));
    }

    private void verifyAddButtonDisabled(){
        verifyThat(triggerForm.addButton(), is(disabled()));
    }

    private static void addBushyTail(TriggerForm triggerForm){
        int triggerCount = triggerForm.getNumberOfTriggers();
        triggerForm.addTrigger("bushy tail");
        triggerCount += 2;
        assertThat(triggerForm.getTriggersAsStrings(), hasSize(triggerCount));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("bushy"));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("tail"));

        triggerForm.removeTrigger("tail");
        triggerCount--;
        assertThat(triggerForm.getTriggersAsStrings(), hasSize(triggerCount));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("bushy"));
        assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("tail")));
    }

    private void addFinalTrigger(String finalTrigger) {
        triggerForm.typeTriggerWithoutSubmit("a");
        verifyThat("error message is cleared", triggerForm.getTriggerError(), isEmptyOrNullString());
        verifyThat(triggerForm.addButton(), not(disabled()));
        triggerForm.addTrigger(finalTrigger);
        verifyThat("can add valid trigger", triggerForm.getNumberOfTriggers(), is(++numberOfTriggers));
    }
}
