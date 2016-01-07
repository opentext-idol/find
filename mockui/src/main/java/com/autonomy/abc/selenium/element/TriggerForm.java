package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;

public class TriggerForm extends AppElement {
    public TriggerForm(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    /* Adding triggers */
    private FormInput triggerAddBox() {
        return new FormInput(findElement(By.name("words")), getDriver());
    }

    public void addTrigger(String trigger) {
        triggerAddBox().setAndSubmit(trigger);
        Waits.loadOrFadeWait();
    }

    public WebElement addButton(){
        return findElement(By.tagName("button"));
    }

    public void typeTriggerWithoutSubmit(String trigger){
        triggerAddBox().setValue(trigger);
    }

    public void typeTriggerWithoutSubmit(Keys... keys){
        triggerAddBox().getElement().sendKeys(keys);
    }

    public void clearTriggerBox() {
        triggerAddBox().clear();
    }

    public String getTextInTriggerBox(){
        return triggerAddBox().getValue();
    }

    /* Removing triggers */
    public void removeTrigger(String trigger) {
        trigger(trigger).removeAndWait();
    }

    public void removeTriggerAsync(String trigger) {
        trigger(trigger).removeAsync();
    }

    /* Getting triggers */
    private Removable trigger(final String triggerName) {
        return new LabelBox(findElement(By.cssSelector("[data-id='" + triggerName.toLowerCase().replace("\"","") + "']")), getDriver());
    }

    public void clickTrigger(String trigger) {
        trigger(trigger).click();
    }

    public List<String> getTriggersAsStrings(){
        return ElementUtil.getTexts(triggers());
    }

    public List<Removable> getTriggers(){
        final List<Removable> triggers = new ArrayList<>();
        for (final WebElement trigger : triggers()) {
            triggers.add(new LabelBox(trigger, getDriver()));
        }
        return triggers;
    }

    public int getNumberOfTriggers(){
        return triggers().size();
    }

    private List<WebElement> triggers(){
        return findElements(By.className("term"));
    }

    public String getTriggerError() {
        try {
            return findElement(By.cssSelector(".help-block")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
