/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import lombok.Data;

@Data
public class Marker {
    double x, y;
    String text;
    boolean cluster;
    String color;
    String fontColor;
    boolean fade;
}
