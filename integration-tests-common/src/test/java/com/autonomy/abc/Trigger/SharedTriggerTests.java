package com.autonomy.abc.Trigger;

import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.util.Errors;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class SharedTriggerTests {
    private int numberOfTriggers;
    private final String initialTrigger = "dog";
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
    public static int badTriggersTest(TriggerForm triggerForm){
        SharedTriggerTests sharedTriggerTests = new SharedTriggerTests(triggerForm);

        if(!triggerForm.getTriggersAsStrings().contains(sharedTriggerTests.initialTrigger)){
            triggerForm.addTrigger(sharedTriggerTests.initialTrigger);
            sharedTriggerTests.numberOfTriggers++;
        }

        assertThat(triggerForm.getNumberOfTriggers(), is(sharedTriggerTests.numberOfTriggers));

        sharedTriggerTests.checkBadTriggers();

        triggerForm.typeTriggerWithoutSubmit("a");
        verifyThat("error message is cleared", triggerForm.getTriggerError(), isEmptyOrNullString());
        verifyThat(triggerForm.addButton(), not(disabled()));
        triggerForm.addTrigger("\"Valid Trigger\"");
        verifyThat("can add valid trigger", triggerForm.getNumberOfTriggers(), is(++sharedTriggerTests.numberOfTriggers));
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
        numberOfTriggers++;
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

    private void checkBadTriggers(String[] triggers, String errorSubstring) {
        for (String trigger : triggers) {
            triggerForm.addTrigger(trigger);
            verifyTriggerNotAdded(trigger, errorSubstring);
        }
    }

    private void verifyTriggerNotAdded(String trigger, String errorSubstring){
        verifyNoNewTriggers(trigger);
        verifyErrorString(errorSubstring);
        verifyAddButtonDisabled();
    }

    private void verifyNoNewTriggers(String trigger){
        verifyThat("trigger '" + trigger + "' not added", triggerForm.getNumberOfTriggers(), is(numberOfTriggers));
    }

    private void verifyErrorString(String errorSubstring){
        verifyThat(triggerForm.getTriggerError(), containsString(errorSubstring));
    }

    private void verifyAddButtonDisabled(){
        verifyThat(triggerForm.addButton(), is(disabled()));
    }

    private static void addBushyTail(TriggerForm triggerForm){
        triggerForm.addTrigger("bushy tail");
        assertThat(triggerForm.getTriggersAsStrings(), hasSize(2));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("bushy"));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("tail"));

        triggerForm.removeTrigger("tail");
        assertThat(triggerForm.getTriggersAsStrings(), hasSize(1));
        assertThat(triggerForm.getTriggersAsStrings(), hasItem("bushy"));
        assertThat(triggerForm.getTriggersAsStrings(), not(hasItem("tail")));
    }
}
