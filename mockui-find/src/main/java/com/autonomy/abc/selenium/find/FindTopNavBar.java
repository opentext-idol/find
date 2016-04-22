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
    private WebDriver driver;
    private WebElement header;
    private WebElement inputContainer;
    private FormInput input;

    FindTopNavBar(WebDriver driver) {
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

    void search(String term) {
        input.clear();
        input.setAndSubmit(term);
    }

    public String getSearchBoxTerm() {
        return input.getValue();
    }

    public String getCurrentUsername() {
        return header.findElement(By.className("navbar-username")).getText();
    }

    public List<Removable> additionalConcepts() {
        List<Removable> removables = new ArrayList<>();
        for (WebElement concept : additionalConceptElements()) {
            removables.add(new HPRemovable(concept, driver));
        }
        return removables;
    }

    public Removable additionalConcept(String conceptText) {
        WebElement concept = ElementUtil.ancestor(inputContainer.findElement(new Locator()
                .havingClass("selected-related-concept")
                .containingCaseInsensitive(conceptText)), 1);
        return new HPRemovable(concept, driver);
    }

    public List<String> getAlsoSearchingForTerms() {
        return ElementUtil.getTexts(additionalConceptElements());
    }

    private List<WebElement> additionalConceptElements() {
        return inputContainer.findElements(By.className("selected-related-concept"));
    }
}
