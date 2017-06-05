/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.AbstractConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationUtils;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@JsonDeserialize(builder = AssetConfig.AssetConfigBuilder.class)
public class AssetConfig extends AbstractConfig<AssetConfig> {

    @Getter(AccessLevel.NONE)
    @JsonProperty
    private final Map<AssetType, String> assets;

    String getAssetPath(final AssetType type) {
        return assets.get(type);
    }

    @Override
    public AssetConfig merge(final AssetConfig other) {
        // default merge requires getAssets to be public
        return builder()
                .assets(ConfigurationUtils.mergeMap(assets, other.assets))
                .build();
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class AssetConfigBuilder {

        @SuppressWarnings({"unused", "FieldMayBeFinal"})
        private Map<AssetType, String> assets = new LinkedHashMap<>();

    }

}
