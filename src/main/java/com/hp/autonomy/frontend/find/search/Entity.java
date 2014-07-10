package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.Setter;

@Data
@JsonDeserialize(builder = Entity.Builder.class)
public class Entity {

    private final String text;
    private final int docsWithPhrase;
    private final int occurrences;
    private final int docsWithAllTerms;
    private final int cluster;

    private Entity(final String text, final int docsWithPhrase, final int occurrences, final int docsWithAllTerms, final int cluster) {
        this.text = text;
        this.docsWithPhrase = docsWithPhrase;
        this.occurrences = occurrences;
        this.docsWithAllTerms = docsWithAllTerms;
        this.cluster = cluster;
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String text;
        private int docsWithPhrase;
        private int occurrences;
        private int docsWithAllTerms;
        private int cluster;

        public Entity build() {
            return new Entity(text, docsWithPhrase, occurrences, docsWithAllTerms, cluster);
        }

        @JsonProperty("docs_with_phrase")
        public void setDocsWithPhrase(final int docsWithPhrase) {
            this.docsWithPhrase = docsWithPhrase;
        }

        @JsonProperty("docs_with_all_terms")
        public void setDocsWithAllTerms(final int docsWithAllTerms) {
            this.docsWithAllTerms = docsWithAllTerms;
        }
    }
}
