/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import lombok.Data;

@Data
public class TableData extends ComposableElement {

    String[] cells;

    int rows;
    int cols;

    public boolean validateInput() {
        return rows > 0 && cols > 0 && cells != null && cells.length == rows * cols;
    }
}