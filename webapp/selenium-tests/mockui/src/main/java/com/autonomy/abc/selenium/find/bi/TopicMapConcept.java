package com.autonomy.abc.selenium.find.bi;

import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TopicMapConcept {
    private final WebElement entity;
    private Double[][] boundaries;


    TopicMapConcept(WebElement element) {
        entity = element;
        boundaries = this.getEntityCoordinates();
    }

    Double[][] getBoundaries() {
        return boundaries;
    }

    private Double[][] getEntityCoordinates() {
        final String path = entity.getAttribute("d");

        List<String> coordinatesAsStrings = new LinkedList<>(Arrays.asList(path.split("M|L|Z")));
        coordinatesAsStrings.remove("");

        Double[][] boundaries = {{10000000., -1.}, {10000000., -1.}};

        for(String value : coordinatesAsStrings) {
            //In IE coordinates are separated by a space vs. a comma in other browsers.
            List<Double> pair = Arrays.asList(value.trim().split(",|\\s"))
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

    private void findBoundaryValues(final Double value, Double[] boundaries) {
        if(value > boundaries[1]) {
            boundaries[1] = value;
        }
        else if(value < boundaries[0]) {
            boundaries[0] = value;
        }
    }
}