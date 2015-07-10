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
import com.hp.autonomy.frontend.find.search.IndexesService;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.Resource;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
@JsonDeserialize(builder = IodConfig.Builder.class)
public class IodConfig implements ConfigurationComponent {

    private final String apiKey;
    private final List<Resource> activeIndexes;

    private IodConfig(final String apiKey, final List<Resource> activeIndexes) {
        this.apiKey = apiKey;
        this.activeIndexes = activeIndexes;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public ValidationResult<?> validate(final IndexesService indexesService, final AuthenticationService authenticationService) {
        try {
            if(StringUtils.isBlank(apiKey)) {
                return new ValidationResult<>(false, "API Key is blank");
            }

            //TODO the settings page should prompt for application and domain
            final TokenProxy tokenProxy = authenticationService.authenticateApplication(new ApiKey(apiKey), "IOD-TEST-APPLICATION", "IOD-TEST-DOMAIN", TokenType.simple);

            final Resources indexes = indexesService.listIndexes(tokenProxy);
            final List<Resource> activeIndexes = indexesService.listActiveIndexes();

            return new ValidationResult<>(true, new IndexResponse(indexes, activeIndexes));
        } catch (final HodErrorException e) {
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
        private List<Resource> activeIndexes;

        public IodConfig build() {
            return new IodConfig(apiKey, activeIndexes);
        }
    }

    @Data
    private static class IndexResponse {
        private final Resources indexes;
        private final List<Resource> activeIndexes;

        private IndexResponse(final Resources indexes, final List<Resource> activeIndexes) {
            this.indexes = indexes;
            this.activeIndexes = activeIndexes;
        }
    }
}
