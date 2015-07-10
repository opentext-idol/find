package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
public class ParametricRequest {

    private final Set<ResourceIdentifier> databases;
    private final String queryText;
    private final String fieldText;

    private ParametricRequest(final Set<ResourceIdentifier> databases, final String queryText, final String fieldText) {
        this.databases = databases;
        this.queryText = queryText;
        this.fieldText = fieldText;
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private Set<ResourceIdentifier> databases;
        private String queryText;
        private String fieldText;

        public ParametricRequest build() {
             return new ParametricRequest(databases, queryText, fieldText);
        }

    }
}
