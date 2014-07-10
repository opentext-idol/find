package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@JsonDeserialize(builder = Document.Builder.class)
public class Document {

    private final String reference;
    private final double weight;
    private final List<String> links;
    private final String index;
    private final String title;
    private final String summary;

    private Document(final String reference, final double weight, final List<String> links, final String index, final String title, final String summary) {
        this.reference = reference;
        this.weight = weight;
        this.links = links;
        this.index = index;
        this.title = title;
        this.summary = summary;
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private String reference;
        private double weight;
        private List<String> links;
        private String index;
        private String title;
        private String summary;

        public Document build() {
            return new Document(reference, weight, links, index, title, summary);
        }

    }
}
