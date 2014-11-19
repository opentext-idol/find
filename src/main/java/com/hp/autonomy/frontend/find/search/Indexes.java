package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = Indexes.Builder.class)
public class Indexes {

    private final List<Index> publicIndex;
    private final List<PrivateIndex> privateIndexes;

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        @JsonProperty("public_index")
        private List<Index> publicIndex;

        @JsonProperty("index")
        private List<PrivateIndex> privateIndexes;

        public Indexes build() {
            return new Indexes(publicIndex, privateIndexes);
        }

    }
}
