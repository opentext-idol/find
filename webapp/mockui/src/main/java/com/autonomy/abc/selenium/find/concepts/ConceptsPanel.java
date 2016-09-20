/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.concepts;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.element.HPRemovable;
import com.hp.autonomy.frontend.selenium.element.Removable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the concept view in the left side panel.
 */
public class ConceptsPanel {
    static final String SELECTED_RELATED_CONCEPT_CLASS = "selected-related-concept";

    private final WebElement panel;
    private final WebDriver driver;

    public ConceptsPanel(final WebDriver driver) {
        panel = Container.LEFT.findUsing(driver).findElement(By.xpath(".//h3[contains(text(), 'Concepts')]/.."));
        this.driver = driver;
    }

    /**
     * Get a WebElement for each selected concept cluster, including both single concepts and clusters.
     * @return Selected concept elements
     */
    public List<WebElement> selectedConceptElements() {
        return panel.findElements(By.className(SELECTED_RELATED_CONCEPT_CLASS));
    }

    /**
     * Get the first word in each selected concept cluster in lower case.
     * @return Lower-cased primary selected concepts
     */
    public List<String> selectedConceptHeaders() {
        return selectedConceptElements().stream()
                .map(((Function<WebElement, String>) WebElement::getText).andThen(String::toLowerCase))
                .collect(Collectors.toList());
    }

    /**
     * @return A removable for each of the {@link #selectedConceptElements()}
     */
    public List<Removable> selectedConceptRemovables() {
        return selectedConceptElements().stream()
                .map(concept -> new HPRemovable(concept, driver))
                .collect(Collectors.toList());
    }

    /**
     * @param headerConcept The primary concept in the cluster
     * @return A removable for the concept cluster
     */
    public Removable removableConceptForHeader(final String headerConcept) {
        final String lowerCaseHeader = headerConcept.toLowerCase();

        final Optional<WebElement> match = selectedConceptElements().stream()
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
    public void removeFirstConceptCluster(){
        final List<Removable> removables = selectedConceptRemovables();

        if (removables.isEmpty()) {
            throw new IllegalStateException("There are no concepts to remove");
        } else {
            removables.get(0).removeAndWait();
        }
    }
}
