/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.find.hod.databases.FindHodDatabasesService;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
@JsonDeserialize(builder = IodConfig.Builder.class)
public class IodConfig implements ConfigurationComponent {

    private final String apiKey;
    private final String application;
    private final String domain;
    private final List<ResourceIdentifier> activeIndexes;
    private final Boolean publicIndexesEnabled;

    private IodConfig(final String apiKey, final String application, final String domain, final List<ResourceIdentifier> activeIndexes, final Boolean publicIndexesEnabled) {
        this.apiKey = apiKey;
        this.application = application;
        this.domain = domain;
        this.activeIndexes = activeIndexes;
        this.publicIndexesEnabled = publicIndexesEnabled;
    }

    public List<ResourceIdentifier> getActiveIndexes() {
        return activeIndexes == null ? Collections.<ResourceIdentifier>emptyList() : new ArrayList<>(activeIndexes);
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public ValidationResult<?> validate(final FindHodDatabasesService findHodDatabasesService, final AuthenticationService authenticationService, final List<ResourceIdentifier> activeIndexes) {
        try {
            if (StringUtils.isBlank(apiKey)) {
                return new ValidationResult<>(false, "API Key is blank");
            }

            if (StringUtils.isBlank(apiKey)) {
                return new ValidationResult<>(false, "Application is blank");
            }

            if (StringUtils.isBlank(apiKey)) {
                return new ValidationResult<>(false, "Domain is blank");
            }

            final TokenProxy<EntityType.Application, TokenType.Simple> tokenProxy = authenticationService.authenticateApplication(
                    new ApiKey(apiKey),
                    application,
                    domain,
                    TokenType.Simple.INSTANCE
            );

            final Resources indexes = findHodDatabasesService.getAllIndexes(tokenProxy);

            return new ValidationResult<>(true, new IndexResponse(indexes, activeIndexes));
        } catch (final HodErrorException e) {
            log.error("Error retrieving indexes", e);
            return new ValidationResult<>(false, "Unable to list indexes");
        }
    }

    public IodConfig merge(final IodConfig iod) {
        if (iod != null) {
            final Builder builder = new Builder();

            builder.setApiKey(apiKey == null ? iod.apiKey : apiKey);
            builder.setApplication(application == null ? iod.application : application);
            builder.setDomain(domain == null ? iod.domain : domain);
            builder.setActiveIndexes(activeIndexes == null ? iod.activeIndexes : activeIndexes);
            builder.setPublicIndexesEnabled(publicIndexesEnabled == null ? iod.publicIndexesEnabled : publicIndexesEnabled);

            return builder.build();
        } else {
            return this;
        }
    }

    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String apiKey;
        private String application;
        private String domain;
        private List<ResourceIdentifier> activeIndexes;
        private Boolean publicIndexesEnabled;

        public IodConfig build() {
            return new IodConfig(apiKey, application, domain, activeIndexes, publicIndexesEnabled);
        }
    }

    @Data
    private static class IndexResponse {
        private final Resources indexes;
        private final List<ResourceIdentifier> activeIndexes;

        private IndexResponse(final Resources indexes, final List<ResourceIdentifier> activeIndexes) {
            this.indexes = indexes;
            this.activeIndexes = activeIndexes;
        }
    }
}
