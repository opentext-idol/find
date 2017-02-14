/*
 * Copyright 2015-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigBuilder;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.ParametricDisplayValues;
import com.hp.autonomy.frontend.find.core.configuration.PowerPointConfig;
import com.hp.autonomy.frontend.find.core.configuration.SavedSearchConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig.IdolFindConfigBuilder;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.user.UserServiceConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings({"InstanceVariableOfConcreteClass", "DefaultAnnotationParam"})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = IdolFindConfigBuilder.class)
public class IdolFindConfig extends AbstractConfig<IdolFindConfig> implements UserServiceConfig, IdolSearchCapable, FindConfig<IdolFindConfig, IdolFindConfigBuilder> {
    private static final String SECTION = "Find Config Root";

    private final CommunityAuthentication login;
    private final ServerConfig content;
    private final QueryManipulation queryManipulation;
    private final ViewConfig view;
    private final AnswerServerConfig answerServer;
    @JsonProperty("savedSearches")
    private final SavedSearchConfig savedSearchConfig;
    private final MMAP mmap;
    private final UiCustomization uiCustomization;
    private final FieldsInfo fieldsInfo;
    private final MapConfiguration map;
    private final Integer minScore;
    private final StatsServerConfig statsServer;
    private final Integer topicMapMaxResults;
    private final PowerPointConfig powerPoint;
    @Singular
    private final Collection<ParametricDisplayValues> parametricDisplayValues;

    @Override
    public IdolFindConfig merge(final IdolFindConfig maybeOther) {
        return Optional.ofNullable(maybeOther)
                .map(other -> builder()
                        .content(content == null ? other.content : content.merge(other.content))
                        .login(login == null ? other.login : login.merge(other.login))
                        .queryManipulation(queryManipulation == null ? other.queryManipulation : queryManipulation.merge(other.queryManipulation))
                        .view(view == null ? other.view : view.merge(other.view))
                        .answerServer(answerServer == null ? other.answerServer : answerServer.merge(other.answerServer))
                        .savedSearchConfig(savedSearchConfig == null ? other.savedSearchConfig : savedSearchConfig.merge(other.savedSearchConfig))
                        .mmap(mmap == null ? other.mmap : mmap.merge(other.mmap))
                        .uiCustomization(uiCustomization == null ? other.uiCustomization : uiCustomization.merge(other.uiCustomization))
                        .fieldsInfo(fieldsInfo == null ? other.fieldsInfo : fieldsInfo.merge(other.fieldsInfo))
                        .map(map == null ? other.map : map.merge(other.map))
                        .minScore(minScore == null ? other.minScore : minScore)
                        .statsServer(statsServer == null ? other.statsServer : statsServer.merge(other.statsServer))
                        .parametricDisplayValues(CollectionUtils.isEmpty(parametricDisplayValues) ? other.parametricDisplayValues : parametricDisplayValues)
                        .topicMapMaxResults(topicMapMaxResults == null ? other.topicMapMaxResults : topicMapMaxResults)
                        .powerPoint(powerPoint == null ? other.powerPoint : powerPoint.merge(other.powerPoint))
                        .build())
                .orElse(this);
    }

    @JsonIgnore
    @Override
    public AciServerDetails getCommunityDetails() {
        return login.getCommunity().toAciServerDetails();
    }

    @JsonIgnore
    @Override
    public Authentication<?> getAuthentication() {
        return login;
    }

    @Override
    public IdolFindConfig withoutDefaultLogin() {
        return toBuilder()
                .login(login.withoutDefaultLogin())
                .build();
    }

    @Override
    public IdolFindConfig generateDefaultLogin() {
        return toBuilder()
                .login(login.generateDefaultLogin())
                .build();
    }

    @Override
    public IdolFindConfig withHashedPasswords() {
        // no work to do yet
        return this;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        login.basicValidate(SECTION);
        content.basicValidate("content");
        savedSearchConfig.basicValidate(SECTION);

        if(powerPoint != null) {
            powerPoint.basicValidate("powerPoint");
        }

        if(map != null) {
            map.basicValidate("map");
        }

        if(queryManipulation != null) {
            queryManipulation.basicValidate(SECTION);
        }

        if(answerServer != null) {
            answerServer.basicValidate("AnswerServer");
        }
    }

    @JsonIgnore
    @Override
    public AciServerDetails getContentAciServerDetails() {
        return content.toAciServerDetails();
    }

    @Override
    @JsonIgnore
    public ViewConfig getViewConfig() {
        return view;
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class IdolFindConfigBuilder implements FindConfigBuilder<IdolFindConfig, IdolFindConfigBuilder> {
        @SuppressWarnings("unused")
        @JsonProperty("savedSearches")
        private SavedSearchConfig savedSearchConfig;
    }
}
