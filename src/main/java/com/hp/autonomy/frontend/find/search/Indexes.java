package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.ArrayList;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = Indexes.Builder.class)
public class Indexes {

    private final List<Index> publicIndexes;
    private final List<PrivateIndex> privateIndexes;

    public List<PrivateIndex> getPrivateIndexes() {
        return new ArrayList<>(privateIndexes);
    }

    public List<Index> getPublicIndexes() {
        return new ArrayList<>(publicIndexes);
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        @JsonProperty("public_index")
        private List<Index> publicIndex = Collections.emptyList();

        @JsonProperty("index")
        private List<PrivateIndex> privateIndexes = Collections.emptyList();

        public Indexes build() {
            return new Indexes(publicIndex, privateIndexes);
        }

    }
}
