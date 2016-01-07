package com.autonomy.abc.Trigger;

import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.util.Errors;
import org.openqa.selenium.Keys;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
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
            "\"dog\""
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
            ",,,,,,"
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
    }

    public static void badTriggersTest(TriggerForm triggerForm, int initialNumberOfTriggers){
        SharedTriggerTests sharedTriggerTests = new SharedTriggerTests(triggerForm);

        if(!triggerForm.getTriggersAsStrings().contains(sharedTriggerTests.initialTrigger)){
            triggerForm.addTrigger(sharedTriggerTests.initialTrigger);
            initialNumberOfTriggers++;
        }

        sharedTriggerTests.numberOfTriggers = initialNumberOfTriggers;

        assertThat(triggerForm.getNumberOfTriggers(), is(initialNumberOfTriggers));

        sharedTriggerTests.checkBadTriggers();

        triggerForm.typeTriggerWithoutSubmit("a");
        verifyThat("error message is cleared", triggerForm.getTriggerError(), isEmptyOrNullString());
        verifyThat(triggerForm.addButton(), not(disabled()));

        verifyThat("can add valid trigger", triggerForm.getNumberOfTriggers(), is(++initialNumberOfTriggers));
    }

    private void checkBadTriggers(){
        checkBadTriggers(duplicateTriggers, Errors.Term.DUPLICATE_EXISTING);
        checkBadTriggers(quoteTriggers, Errors.Term.QUOTES);
        checkBadTriggers(commaTriggers, Errors.Term.COMMAS);
        checkBadTriggers(caseTriggers, Errors.Term.CASE);
        checkWhitespaceTriggers();
    }

    private void checkWhitespaceTriggers(){
        for(String trigger : whitespaceTriggers){
            triggerForm.addTrigger(trigger);
            verifyThat(triggerForm.addButton(), is(disabled()));
        }

        triggerForm.clear();
        triggerForm.typeTriggerWithoutSubmit(Keys.RETURN);
        verifyThat(triggerForm.addButton(), is(disabled()));
    }

    private void checkBadTriggers(String[] triggers, String errorSubstring) {
        for (String trigger : triggers) {
            triggerForm.addTrigger(trigger);
            verifyThat("trigger '" + trigger + "' not added", triggerForm.getNumberOfTriggers(), is(numberOfTriggers));
            verifyThat(triggerForm.getTriggerError(), containsString(errorSubstring));
            verifyThat(triggerForm.addButton(), disabled());
        }
    }
}
