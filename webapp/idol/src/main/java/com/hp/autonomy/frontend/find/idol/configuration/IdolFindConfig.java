/*
 * Copyright 2015-2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.configuration;

import com.autonomy.aci.client.transport.AciServerDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.authentication.CommunityAuthentication;
import com.hp.autonomy.frontend.configuration.server.ProductType;
import com.hp.autonomy.frontend.configuration.server.ServerConfig;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.FindConfigBuilder;
import com.hp.autonomy.frontend.find.core.configuration.MapConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.MessageOfTheDayConfig;
import com.hp.autonomy.frontend.find.core.configuration.SavedSearchConfig;
import com.hp.autonomy.frontend.find.core.configuration.TrendingConfiguration;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.frontend.find.core.configuration.export.ExportConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig.IdolFindConfigBuilder;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.idol.answer.configuration.AnswerServerConfig;
import com.hp.autonomy.searchcomponents.idol.configuration.IdolSearchCapable;
import com.hp.autonomy.searchcomponents.idol.configuration.QueryManipulation;
import com.hp.autonomy.searchcomponents.idol.view.configuration.ViewConfig;
import com.hp.autonomy.user.UserServiceConfig;
import java.util.Collection;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private final EntitySearchConfig entitySearch;
    @JsonProperty("savedSearches")
    private final SavedSearchConfig savedSearchConfig;
    private final MMAP mmap;
    private final MessageOfTheDayConfig messageOfTheDay;
    private final UiCustomization uiCustomization;
    @JsonProperty("idolFieldPathNormalizerXMLPrefixes")
    private final Collection<String> idolFieldPathNormalizerXMLPrefixes;
    private final FieldsInfo fieldsInfo;
    private final MapConfiguration map;
    private final TrendingConfiguration trending;
    private final Integer minScore;
    private final StatsServerConfig statsServer;
    private final Integer topicMapMaxResults;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer comparisonStoreStateMaxResults;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer exportStoreStateMaxResults;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Integer documentSummaryMaxLength;
    private final ExportConfig export;
    private final String referenceField;

    @JsonIgnore
    private volatile Map<String, Map<Integer, String>> productMap;

    @Override
    public IdolFindConfig merge(final IdolFindConfig maybeOther) {
        return Optional.ofNullable(maybeOther)
            .map(other -> builder()
                .content(content == null ? other.content : content.merge(other.content))
                .login(login == null ? other.login : login.merge(other.login))
                .queryManipulation(queryManipulation == null ? other.queryManipulation : queryManipulation.merge(other.queryManipulation))
                .view(view == null ? other.view : view.merge(other.view))
                .answerServer(answerServer == null ? other.answerServer : answerServer.merge(other.answerServer))
                .entitySearch(entitySearch == null ? other.entitySearch : entitySearch.merge(other.entitySearch))
                .savedSearchConfig(savedSearchConfig == null ? other.savedSearchConfig : savedSearchConfig.merge(other.savedSearchConfig))
                .mmap(mmap == null ? other.mmap : mmap.merge(other.mmap))
                .messageOfTheDay(messageOfTheDay == null ? other.messageOfTheDay : messageOfTheDay.merge(other.messageOfTheDay))
                .uiCustomization(uiCustomization == null ? other.uiCustomization : uiCustomization.merge(other.uiCustomization))
                .idolFieldPathNormalizerXMLPrefixes(CollectionUtils.isEmpty(idolFieldPathNormalizerXMLPrefixes) ? other.idolFieldPathNormalizerXMLPrefixes : idolFieldPathNormalizerXMLPrefixes)
                .fieldsInfo(fieldsInfo == null ? other.fieldsInfo : fieldsInfo.merge(other.fieldsInfo))
                .map(map == null ? other.map : map.merge(other.map))
                .trending(trending == null ? other.trending : trending.merge(other.trending))
                .minScore(minScore == null ? other.minScore : minScore)
                .statsServer(statsServer == null ? other.statsServer : statsServer.merge(other.statsServer))
                .topicMapMaxResults(topicMapMaxResults == null ? other.topicMapMaxResults : topicMapMaxResults)
                .comparisonStoreStateMaxResults(comparisonStoreStateMaxResults == null ? other.comparisonStoreStateMaxResults : comparisonStoreStateMaxResults)
                .exportStoreStateMaxResults(exportStoreStateMaxResults == null ? other.exportStoreStateMaxResults : exportStoreStateMaxResults)
                .documentSummaryMaxLength(documentSummaryMaxLength == null ? other.documentSummaryMaxLength : documentSummaryMaxLength)
                .export(Optional.ofNullable(export).map(exportConfig -> exportConfig.merge(maybeOther.export)).orElse(maybeOther.export))
                .referenceField(referenceField == null ? other.referenceField : referenceField)
                .build())
            .orElse(this);
    }

    // somewhat messy workaround for the fact that default method does not handle @JsonProperty annotations
    @Override
    public Map<String, OptionalConfigurationComponent<?>> getValidationMap() {
        final Map<String, OptionalConfigurationComponent<?>> validationMap = super.getValidationMap();
        if(validationMap.containsKey("savedSearchConfig")) {
            validationMap.put("savedSearches", validationMap.remove("savedSearchConfig"));
        }
        return validationMap;
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
        trending.basicValidate("trending");
        savedSearchConfig.basicValidate(SECTION);

        if(map != null) {
            map.basicValidate("map");
        }

        if(export != null) {
            export.basicValidate(SECTION);
        }

        if(queryManipulation != null) {
            queryManipulation.basicValidate(SECTION);
        }

        if(answerServer != null) {
            answerServer.basicValidate("AnswerServer");
        }

        if(entitySearch != null) {
            entitySearch.basicValidate(EntitySearchConfig.SECTION);
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

    @Override
    public String lookupComponentNameByHostAndPort(final String hostName, final int port) {
        if(productMap == null) {
            final Map<String, Map<Integer, String>> tempProductMap = new HashMap<>();
            addEntriesToProductMap(tempProductMap, ProductType.AXE.getFriendlyName(), content.getHost(), content.getPort(), content.getServicePort());
            addEntriesToProductMap(tempProductMap, ProductType.VIEW.getFriendlyName(), view.getHost(), view.getPort(), view.getServicePort());

            if(!"default".equals(login.getMethod())) {
                addEntriesToProductMap(tempProductMap, ProductType.UASERVER.getFriendlyName(), login.getCommunity().getHost(), login.getCommunity().getPort(), login.getCommunity().getServicePort());
            }

            if(isOptionalComponentEnabled(queryManipulation)) {
                addEntriesToProductMap(tempProductMap, ProductType.QMS.getFriendlyName(), queryManipulation.getServer().getHost(), queryManipulation.getServer().getPort(), queryManipulation.getServer().getServicePort());
            }

            if(isOptionalComponentEnabled(answerServer)) {
                addEntriesToProductMap(tempProductMap, ProductType.ANSWERSERVER.getFriendlyName(), answerServer.getServer().getHost(), answerServer.getServer().getPort(), answerServer.getServer().getServicePort());
            }

            if(isOptionalComponentEnabled(entitySearch)) {
                addEntriesToProductMap(tempProductMap, ProductType.AXE.getFriendlyName(), entitySearch.getServer().getHost(), entitySearch.getServer().getPort(), entitySearch.getServer().getServicePort());
            }

            if(isOptionalComponentEnabled(statsServer)) {
                addEntriesToProductMap(tempProductMap, statsServer.getServer().getHost(), ProductType.STATS.getFriendlyName(), statsServer.getServer().getPort(), statsServer.getServer().getServicePort());
            }

            productMap = tempProductMap;
        }

        return productMap.getOrDefault(hostName, Collections.emptyMap()).get(port);
    }

    private <T extends OptionalConfigurationComponent<T>> Boolean isOptionalComponentEnabled(final OptionalConfigurationComponent<T> maybeComponent) {
        return Optional.ofNullable(maybeComponent)
            .map(component -> BooleanUtils.isTrue(component.getEnabled()))
            .orElse(false);
    }

    private void addEntriesToProductMap(final Map<String, Map<Integer, String>> productMap,
                                        final String productName,
                                        final String hostName,
                                        final Integer... ports) {
        productMap.compute(hostName, (key, maybeMap) -> {
            final Map<Integer, String> map = Optional.ofNullable(maybeMap).orElse(new HashMap<>());
            Arrays.stream(ports)
                .filter(Objects::nonNull)
                .forEach(port -> map.put(port, productName));
            return map;
        });
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class IdolFindConfigBuilder implements FindConfigBuilder<IdolFindConfig, IdolFindConfigBuilder> {
        @SuppressWarnings("unused")
        @JsonProperty("savedSearches")
        private SavedSearchConfig savedSearchConfig;

        @SuppressWarnings("unused")
        @JsonProperty("idolFieldPathNormalizerXMLPrefixes")
        private Collection<String> idolFieldPathNormalizerXMLPrefixes;
    }
}
