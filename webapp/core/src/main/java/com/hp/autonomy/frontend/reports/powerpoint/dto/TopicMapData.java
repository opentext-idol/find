/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint.dto;

import java.util.ArrayList;
import lombok.Data;

@Data
public class TopicMapData implements ComposableElement {

    private Path[] paths;

    @Data
    public static class Path {
        public String name;
        public String color;
        public String color2;
        public double opacity;
        public ArrayList<double[]> points;
    }
}

