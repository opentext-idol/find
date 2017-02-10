/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
public class ReportData {

    private Child[] children;

    @Data
    public static class Child {
        private double x, y, width = 1, height = 1;

        private String title;

        private double margin = 3;
        private double textMargin = 2;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(name = "dategraph", value = DategraphData.class),
                @JsonSubTypes.Type(name = "list", value = ListData.class),
                @JsonSubTypes.Type(name = "map", value = MapData.class),
                @JsonSubTypes.Type(name = "sunburst", value = SunburstData.class),
                @JsonSubTypes.Type(name = "table", value = TableData.class),
                @JsonSubTypes.Type(name = "text", value = TextData.class),
                @JsonSubTypes.Type(name = "topicmap", value = TopicMapData.class)
        })
        private ComposableElement data;
    }

}
