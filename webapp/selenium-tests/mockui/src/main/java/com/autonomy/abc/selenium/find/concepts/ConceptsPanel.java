/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.selenium.find.concepts;

import com.autonomy.abc.selenium.find.Container;
import com.google.common.base.Function;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.HPRemovable;
import com.hp.autonomy.frontend.selenium.element.Removable;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the concept view in the left side panel.
 */
public class ConceptsPanel {
    private static final String SELECTED_RELATED_CONCEPT_CLASS = "selected-related-concept";
    private static final By POPOVER_LOCATOR = By.cssSelector(".selected-concept-container .popover");

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
                .map(WebElement::getText)
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
        selectedConceptRemovables().forEach(Removable::removeAndWait);
    }

    private EditPopover editPopover(final Removable concept) {
        concept.click();

        new WebDriverWait(driver, 5)
                .withMessage("Popover did not open")
                .until(ExpectedConditions.visibilityOfElementLocated(POPOVER_LOCATOR));

        return new EditPopover(panel.findElement(POPOVER_LOCATOR));
    }

    public EditPopover editConcept(final int i) {
        //Necessary due to tooltip
        try {
            return editPopover(selectedConceptRemovables().get(i));
        } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException ignored) {
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
        private final FormInput editBox;

        private EditPopover(final WebElement element) {
            super(element, driver);
            editBox = new FormInput(findElement(By.cssSelector(".edit-concept-form .form-group textarea")), driver);
        }

        public void cancelEdit() {
            findElement(By.cssSelector(".edit-concept-cancel-button")).click();
            new WebDriverWait(driver, 5).until((Function<? super WebDriver, Boolean>) x -> popOverGone());
        }

        public void saveEdit() {
            saveButton().click();
            new WebDriverWait(driver, 5).until((Function<? super WebDriver, Boolean>) x -> popOverGone());
        }

        public WebElement saveButton() {
            return findElement(By.cssSelector(".edit-concept-confirm-button"));
        }

        public boolean containsValue(final CharSequence value) {
            return editBox.getValue().contains(value);
        }

        public void setValue(final String value) {
            editBox.setValue(value);
        }

        public void setValueAndSave(final Iterable<String> concepts) {
            editBox.clear();
            final WebElement box = editBox.getElement();

            for (final String concept : concepts) {
                box.sendKeys(concept);
                box.sendKeys(Keys.ENTER);
            }
            saveEdit();
        }
    }
}
