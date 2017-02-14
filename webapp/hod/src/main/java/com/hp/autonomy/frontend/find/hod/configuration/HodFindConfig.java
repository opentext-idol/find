/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.passwords.PasswordsConfig;
import com.hp.autonomy.frontend.configuration.redis.RedisConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigBuilder;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.ParametricDisplayValues;
import com.hp.autonomy.frontend.find.core.configuration.PowerPointConfig;
import com.hp.autonomy.frontend.find.core.configuration.SavedSearchConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.sso.HodSsoConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.hod.configuration.HodSearchCapable;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.jasypt.util.text.TextEncryptor;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

@SuppressWarnings({"InstanceVariableOfConcreteClass", "DefaultAnnotationParam"})
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = HodFindConfig.HodFindConfigBuilder.class)
public class HodFindConfig extends AbstractConfig<HodFindConfig> implements HodSearchCapable, PasswordsConfig<HodFindConfig>, HodSsoConfig, FindConfig<HodFindConfig, HodFindConfig.HodFindConfigBuilder> {
    private static final String SECTION = "Hod Config";

    private final Authentication<?> login;
    private final HsodConfig hsod;
    private final HodConfig hod;
    private final QueryManipulationConfig queryManipulation;
    private final Set<String> allowedOrigins;
    private final RedisConfig redis;
    private final FieldsInfo fieldsInfo;
    private final MapConfiguration map;
    private final UiCustomization uiCustomization;
    private final Integer minScore;
    @Singular
    private final Collection<ParametricDisplayValues> parametricDisplayValues;
    private final Integer topicMapMaxResults;
    private final PowerPointConfig powerPoint;

    @JsonProperty("savedSearches")
    private final SavedSearchConfig savedSearchConfig;

    @SuppressWarnings("OverlyComplexMethod")
    @Override
    public HodFindConfig merge(final HodFindConfig config) {
        return config != null ? builder()
                .login(login == null ? config.login : login.merge(config.login))
                .hod(hod == null ? config.hod : hod.merge(config.hod))
                .allowedOrigins(allowedOrigins == null ? config.allowedOrigins : allowedOrigins)
                .redis(redis == null ? config.redis : redis.merge(config.redis))
                .queryManipulation(queryManipulation == null ? config.queryManipulation : queryManipulation.merge(config.queryManipulation))
                .hsod(hsod == null ? config.hsod : hsod.merge(config.hsod))
                .fieldsInfo(fieldsInfo == null ? config.fieldsInfo : fieldsInfo.merge(config.fieldsInfo))
                .map(map == null ? config.map : map.merge(config.map))
                .uiCustomization(uiCustomization == null ? config.uiCustomization : uiCustomization.merge(config.uiCustomization))
                .savedSearchConfig(savedSearchConfig == null ? config.savedSearchConfig : savedSearchConfig.merge(config.savedSearchConfig))
                .minScore(minScore == null ? config.minScore : minScore)
                .parametricDisplayValues(parametricDisplayValues == null ? config.parametricDisplayValues : parametricDisplayValues)
                .topicMapMaxResults(topicMapMaxResults == null ? config.topicMapMaxResults : topicMapMaxResults)
                .powerPoint(powerPoint == null ? config.powerPoint : powerPoint.merge(config.powerPoint))
                .build() : this;
    }

    @Override
    public HodFindConfig withoutDefaultLogin() {
        final HodFindConfigBuilder builder = toBuilder();
        builder.login = builder.login.withoutDefaultLogin();
        return builder.build();
    }

    @Override
    public HodFindConfig generateDefaultLogin() {
        final HodFindConfigBuilder builder = toBuilder();
        builder.login = builder.login.generateDefaultLogin();
        return builder.build();
    }

    @Override
    public HodFindConfig withHashedPasswords() {
        final HodFindConfigBuilder builder = toBuilder();
        builder.login = builder.login.withHashedPasswords();
        return builder.build();
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        redis.basicValidate(SECTION);
        queryManipulation.basicValidate(SECTION);
        savedSearchConfig.basicValidate(SECTION);

        if (powerPoint != null) {
            powerPoint.basicValidate("powerPoint");
        }

        if (map != null) {
            map.basicValidate("map");
        }

        if (!"default".equalsIgnoreCase(login.getMethod())) {
            login.basicValidate(SECTION);
        }
    }

    @Override
    public HodFindConfig withoutPasswords() {
        final HodFindConfigBuilder builder = toBuilder();
        builder.login = login.withoutPasswords();
        return builder.build();
    }

    @Override
    public HodFindConfig withEncryptedPasswords(final TextEncryptor encryptor) {
        return this;
    }

    @Override
    public HodFindConfig withDecryptedPasswords(final TextEncryptor encryptor) {
        return this;
    }

    @Override
    @JsonIgnore
    public Authentication<?> getAuthentication() {
        return login;
    }

    @Override
    @JsonIgnore
    public ApiKey getApiKey() {
        return hod.getApiKey();
    }

    @Override
    public URL getSsoUrl() {
        return hod.getSsoPageGetUrl();
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class HodFindConfigBuilder implements FindConfigBuilder<HodFindConfig, HodFindConfigBuilder> {
        @SuppressWarnings("unused")
        @JsonProperty("savedSearches")
        private SavedSearchConfig savedSearchConfig;
    }
}
