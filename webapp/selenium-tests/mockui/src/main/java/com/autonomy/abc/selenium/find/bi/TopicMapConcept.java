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

package com.autonomy.abc.selenium.find.bi;

import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TopicMapConcept {
    private final WebElement entity;
    private final Double[][] boundaries;

    TopicMapConcept(final WebElement element) {
        entity = element;
        boundaries = getEntityCoordinates();
    }

    Double[][] getBoundaries() {
        return boundaries;
    }

    private Double[][] getEntityCoordinates() {
        final String path = entity.getAttribute("d");

        final List<String> coordinatesAsStrings = new LinkedList<>(Arrays.asList(path.split("M|L|Z")));
        coordinatesAsStrings.remove("");

        final Double[][] boundaries = {{10000000., -1.}, {10000000., -1.}};

        for(final String value : coordinatesAsStrings) {
            //In IE coordinates are separated by a space vs. a comma in other browsers.
            final List<Double> pair = Arrays.asList(value.trim().split(",|\\s"))
                    .stream()
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());

            //Boundary Values: x axis
            findBoundaryValues(pair.get(0), boundaries[0]);
            //Boundary Values: y axis
            findBoundaryValues(pair.get(1), boundaries[1]);
        }
        return boundaries;
    }

    private void findBoundaryValues(final Double value, final Double[] boundaries) {
        if(value > boundaries[1]) {
            boundaries[1] = value;
        } else if(value < boundaries[0]) {
            boundaries[0] = value;
        }
    }
}
