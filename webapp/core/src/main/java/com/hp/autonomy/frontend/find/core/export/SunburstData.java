/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import lombok.Data;

@Data
public class SunburstData extends ComposableElement {
    private String[] categories;
    private double[] values;

    public boolean validateInput() {
        return categories != null && values != null && categories.length == values.length;
    }
}