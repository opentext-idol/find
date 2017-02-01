/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import lombok.Data;

@Data
public class Document {
    String title;
    String date;
    String ref;
    String summary;

    public Document() {
    }

    public Document(final String title, final String date, final String ref, final String summary) {
        this.title = title;
        this.date = date;
        this.ref = ref;
        this.summary = summary;
    }
}