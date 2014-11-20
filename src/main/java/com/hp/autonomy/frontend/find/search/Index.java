package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@JsonDeserialize(builder = Index.Builder.class)
public class Index {
    private final String index;
    private final String type;

    private Index(final String index, final String type) {
        this.index = index;
        this.type = type;
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        private String index;
        private String type;

        public Index build() {
            return new Index(index, type);
        }

    }
}
