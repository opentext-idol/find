/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.iod.client.api.textindexing.Index;
import com.hp.autonomy.iod.client.api.textindexing.Indexes;
import com.hp.autonomy.frontend.find.search.IndexesService;
import java.util.List;

import com.hp.autonomy.iod.client.error.IodErrorException;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.client.RestClientException;

@Data
@JsonDeserialize(builder = IodConfig.Builder.class)
public class IodConfig implements ConfigurationComponent {

    private final String apiKey;
    private final List<Index> activeIndexes;

    private IodConfig(final String apiKey, final List<Index> activeIndexes) {
        this.apiKey = apiKey;
        this.activeIndexes = activeIndexes;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public ValidationResult<?> validate(final IndexesService indexesService) {
        try {
            if(StringUtils.isBlank(apiKey)) {
                return new ValidationResult<>(false, "API Key is blank");
            }

            final Indexes indexes = indexesService.listIndexes(apiKey);
            final List<Index> activeIndexes = indexesService.listActiveIndexes();

            return new ValidationResult<>(true, new IndexResponse(indexes, activeIndexes));
        } catch (IodErrorException e) {
            return new ValidationResult<>(false, "Unable to list indexes");
        }
    }

    public IodConfig merge(final IodConfig iod) {
        if(iod != null) {
            final Builder builder = new Builder();

            builder.setApiKey(this.apiKey == null ? iod.apiKey : this.apiKey);
            builder.setActiveIndexes(this.activeIndexes == null ? iod.activeIndexes : this.activeIndexes);

            return builder.build();
        }
        else {
            return this;
        }
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String apiKey;
        private List<Index> activeIndexes;

        public IodConfig build() {
            return new IodConfig(apiKey, activeIndexes);
        }
    }

    @Data
    private static class IndexResponse {
        private final Indexes indexes;
        private final List<Index> activeIndexes;

        private IndexResponse(final Indexes indexes, final List<Index> activeIndexes) {
            this.indexes = indexes;
            this.activeIndexes = activeIndexes;
        }
    }
}
