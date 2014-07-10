package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = Documents.Builder.class)
public class Documents {

    private final List<Document> documents;

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private List<Document> documents;

        public Documents build() {
            return new Documents(documents);
        }

    }

}
