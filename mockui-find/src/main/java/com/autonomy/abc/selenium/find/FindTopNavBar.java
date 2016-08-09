package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.HPRemovable;
import com.hp.autonomy.frontend.selenium.element.Removable;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class FindTopNavBar implements LoginService.LogoutHandler {
    private final WebDriver driver;
    private final WebElement header;
    private final WebElement inputContainer;
    private final FormInput input;

    public FindTopNavBar(final WebDriver driver) {
        this.driver = driver;
        this.header = driver.findElement(By.className("header"));
        this.inputContainer = driver.findElement(By.className("input-view-container"));
        this.input = new FormInput(inputContainer.findElement(By.className("find-input")), driver);
    }

    @Override
    public void logOut() {
        header.findElement(By.className("hp-settings")).click();
        header.findElement(By.className("navigation-logout")).click();
        Waits.loadOrFadeWait();
    }

    void search(final String term) {
        input.clear();
        /*input.setValue(term);
        Waits.loadOrFadeWait();
        input.submit();*/
        input.setAndSubmit(term);

    }

    public String getSearchBoxTerm() {
        return input.getValue();
    }

    public String getCurrentUsername() {
        return header.findElement(By.className("navbar-username")).getText();
    }

    public List<Removable> additionalConcepts() {
        final List<Removable> removables = new ArrayList<>();
        for (final WebElement concept : additionalConceptElements()) {
            removables.add(new HPRemovable(concept, driver));
        }
        return removables;
    }

    public Removable additionalConcept(final String conceptText) {
        final WebElement concept = ElementUtil.ancestor(inputContainer.findElement(new Locator()
                .havingClass("selected-related-concept")
                .containingCaseInsensitive(conceptText)), 3);
        return new HPRemovable(concept, driver);
    }

    public void closeFirstConcept(){
        final WebElement firstConcept = inputContainer.findElement(By.cssSelector(".additional-concepts div:first-child"));
        firstConcept.findElement(By.className("hp-close")).click();
    }

    public List<String> getAlsoSearchingForTerms() {
        if(scrollButtonsExists()) {
            additionalConceptsScrollAllTheWayLeft();
        }
        List<String> badFormatText = new ArrayList<>();
        for(WebElement concept:additionalConceptElements()){
            if(!concept.isDisplayed()){
                additionalConceptsScrollRight(concept);
            }
            badFormatText.add(concept.getText());
        }
        final List<String> goodFormatText = new ArrayList<>();
        for(final String entry: badFormatText){goodFormatText.add(entry.toLowerCase());}
        return goodFormatText;
    }

    private void additionalConceptsScrollRight(WebElement concept) {
        int i = 0;
        while (concept.getText().equals("") && i<20) {
            inputContainer.findElement(By.cssSelector("button.right-scroll")).click();
            i++;
        }
    }
    private void additionalConceptsScrollAllTheWayLeft(){
        for(int i=0;i<additionalConceptElements().size()*2;i++) {
            inputContainer.findElement(By.cssSelector("button.left-scroll")).click();
        }
    }

    private List<WebElement> additionalConceptElements() {
        return inputContainer.findElements(By.className("selected-related-concept"));
    }

    private boolean scrollButtonsExists(){
        return !inputContainer.findElements(By.className("scrolling-buttons")).isEmpty();
    }
}
