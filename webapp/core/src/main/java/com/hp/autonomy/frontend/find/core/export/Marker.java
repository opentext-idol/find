package com.hp.autonomy.frontend.find.core.export;

import lombok.Data;

/*
 * $Id:$
 *
 * Copyright (c) 2017, Autonomy Systems Ltd.
 *
 * Last modified by $Author$ on $Date$ 
 */
@Data
public class Marker {
    double x, y;
    String text;
    boolean cluster;
    String color;
    String fontColor;
}
