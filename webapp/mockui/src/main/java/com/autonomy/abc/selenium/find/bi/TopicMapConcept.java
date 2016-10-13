package com.autonomy.abc.selenium.find.bi;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TopicMapConcept {

    private final WebElement entity;
    private Double[][] boundaries;

    TopicMapConcept(WebElement element) {
        entity = element;
    }

    public Double[][] getBoundaries() {
        return boundaries;
    }

    Double[][] extractLocations() {
        final String path = entity.getAttribute("d");

        List<ImmutablePair> coords = new ArrayList<>();

        List<String> coordinates = new LinkedList<>(Arrays.asList(path.split("M|L|Z")));
        coordinates.remove("");

        for(String value : coordinates){
            String[] pair = value.split(",");
            coords.add(new ImmutablePair(Double.parseDouble(pair[0]),Double.parseDouble(pair[1])));
        }

        double lowestX = 100000000;
        double highestX = -1;
        double lowestY = 100000000;
        double highestY = -1;

        for(ImmutablePair coord : coords) {
            //L: x value
            if((double)coord.getLeft() > highestX) {
                highestX = (double)coord.getLeft();
            }
            else if((double)coord.getLeft() < lowestX){
                lowestX = (double)coord.getLeft();
            }

            //R: y value
            if((double)coord.getRight() > highestY) {
                highestY = (double)coord.getRight();
            }
            else if((double)coord.getRight() < lowestY){
                lowestY = (double)coord.getRight();
            }
        }
        Double[][] array = {{lowestX,highestX},{lowestY,highestY}};
        this.boundaries = array;
        return array;
    }
}