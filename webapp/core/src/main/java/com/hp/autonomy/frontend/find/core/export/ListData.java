/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import lombok.Data;

@Data
public class ListData implements ComposableElement {

    private Document[] docs;

    @Data
    public static class Document {
        private String title;
        private String date;
        private String ref;
        private String summary;
        private String thumbnail;
    }
}