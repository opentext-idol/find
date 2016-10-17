/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.concepts;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.HPRemovable;
import com.hp.autonomy.frontend.selenium.element.Removable;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the concept view in the left side panel.
 */
public class ConceptsPanel {
    private static final String SELECTED_RELATED_CONCEPT_CLASS = "selected-related-concept";
    final By POPOVER_LOCATOR = By.cssSelector(".selected-concept-container .popover");


    private final WebElement panel;
    private final WebDriver driver;

    public ConceptsPanel(final WebDriver driver) {
        panel = Container.LEFT.findUsing(driver).findElement(By.cssSelector(".left-side-concepts-view-section"));
        this.driver = driver;
    }

    public FormInput getConceptBoxInput() {
        // Not in constructor as it does not exist for non-BI user
        // We should find a better way of dealing with this sort of problem
        // Sub-classing does not really make sense as it is not extensible to the fully granular functionality model
        return new FormInput(Container.LEFT.findUsing(driver).findElement(By.cssSelector(".concept-view-container .find-input")), driver);
    }

    /**
     * Get a WebElement for each selected concept cluster, including both single concepts and clusters.
     *
     * @return Selected concept elements
     */
    public List<WebElement> selectedConcepts() {
        return panel.findElements(By.className(SELECTED_RELATED_CONCEPT_CLASS));
    }

    /**
     * Get the first word in each selected concept cluster in lower case.
     *
     * @return Lower-cased primary selected concepts
     */
    public List<String> selectedConceptHeaders() {
        return selectedConcepts().stream()
                .map(((Function<WebElement, String>) WebElement::getText).andThen(String::toLowerCase))
                .collect(Collectors.toList());
    }

    /**
     * @return A removable for each of the {@link #selectedConcepts()}
     */
    private List<Removable> selectedConceptRemovables() {
        return selectedConcepts().stream()
                .map(concept -> new HPRemovable(concept, driver))
                .collect(Collectors.toList());
    }

    /**
     * @param headerConcept The primary concept in the cluster
     * @return A removable for the concept cluster
     */
    public Removable removableConceptForHeader(final String headerConcept) {
        final String lowerCaseHeader = headerConcept.toLowerCase();

        final Optional<WebElement> match = selectedConcepts().stream()
                .filter(element -> element.getText().toLowerCase().contains(lowerCaseHeader))
                .findFirst();

        if (match.isPresent()) {
            return new HPRemovable(match.get(), driver);
        } else {
            throw new IllegalStateException("Concept not found");
        }
    }

    /**
     * Remove the first concept cluster.
     */
    public void removeFirstConceptCluster() {
        final List<Removable> removables = selectedConceptRemovables();

        if (removables.isEmpty()) {
            throw new IllegalStateException("There are no concepts to remove");
        } else {
            removables.get(0).removeAndWait();
        }
    }

    public void removeAllConcepts() {
        selectedConceptRemovables().stream().forEach(Removable::removeAndWait);
    }

    public EditPopover editPopover(Removable concept) {
        concept.click();

        new WebDriverWait(driver,5)
                .withMessage("Popover did not open")
                .until(ExpectedConditions.visibilityOfElementLocated(POPOVER_LOCATOR));

        return new EditPopover(panel.findElement(POPOVER_LOCATOR));
    }

    public EditPopover editConcept(final int i) {
        //Necessary due to tooltip
        try{
            return editPopover(selectedConceptRemovables().get(i));
        }
        catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
            return editPopover(selectedConceptRemovables().get(i));
        }
    }

    public boolean popOverGone() {
        return panel.findElements(POPOVER_LOCATOR).isEmpty();
    }

    public String toolTipText(final int index) {
        return panel.findElements(By.cssSelector("[data-toggle='tooltip']"))
                .get(index)
                .getAttribute("data-original-title");
    }

    public class EditPopover extends AppElement {
        private FormInput editBox;

        private EditPopover(final WebElement element) {
            super(element,driver);
            editBox =  new FormInput(findElement(By.cssSelector(".edit-concept-form .form-group textarea")), driver);
        };

        public void cancelEdit() {
            findElement(By.cssSelector(".edit-concept-cancel-button")).click();
            new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(final WebDriver driver) {
                    return popOverGone();
                }
            });
        }

        public void saveEdit() {
            findElement(By.cssSelector(".edit-concept-confirm-button")).click();
            new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(final WebDriver driver) {
                    return popOverGone();
                }
            });
        }

        public boolean containsValue(final String value) {
            //Need to remove all the spaces from the editBox value.
            return editBox.getValue().replaceAll("\\s+","").contains(value);
        }

        public void setValue(final String value) {
            editBox.setValue(value);
        }

        public void setValueAndSave(final List<String> concepts) {
            editBox.clear();
            final WebElement box = editBox.getElement();

            for(String concept : concepts) {
                box.sendKeys(concept);
                box.sendKeys(Keys.ENTER);
            }
            saveEdit();
        }
    }

}
