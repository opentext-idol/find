package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

/*
 * $Id:$
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author:$ on $Date:$
 */
@Data
@JsonDeserialize(builder = PrivateIndex.Builder.class)
public class PrivateIndex {

    private final String flavor;
    private final String index;
    private final String type;
    private final int numComponents;
    private final String subType;
    private final String description;

    // this should probably be a Joda DateTime but parsing the IOD output seems non trivial and at the moment we don't need it
    private final String dateCreated;

    private PrivateIndex(final Builder builder) {
        this.flavor = builder.flavor;
        this.index = builder.index;
        this.type = builder.type;
        this.numComponents = builder.numComponents;
        this.dateCreated = builder.dateCreated;
        this.subType = builder.subType;
        this.description = builder.description;
    }

    public Index toIndex() {
        return new Index.Builder()
            .setIndex(index)
            .setType(type)
            .build();
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private String flavor;
        private String index;
        private String type;
        private String description;

        @JsonProperty("subtype")
        private String subType;

        @JsonProperty("num_components")
        private int numComponents;

        @JsonProperty("date_created")
        private String dateCreated;

        public PrivateIndex build() {
            return new PrivateIndex(this);
        }

    }
}
