package com.hp.autonomy.frontend.find.parametricfields;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
public class ParametricRequest {

    private final Set<String> databases;
    private final String queryText;
    private final String fieldText;

    private ParametricRequest(final Set<String> databases, final String queryText, final String fieldText) {
        this.databases = databases;
        this.queryText = queryText;
        this.fieldText = fieldText;
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Set<String> databases;
        private String queryText;
        private String fieldText;

        public ParametricRequest build() {
             return new ParametricRequest(databases, queryText, fieldText);
        }

    }
}
