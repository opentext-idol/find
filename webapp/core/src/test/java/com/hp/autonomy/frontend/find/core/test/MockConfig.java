package com.hp.autonomy.frontend.find.core.test;

import com.hp.autonomy.frontend.configuration.SimpleComponent;
import com.hp.autonomy.frontend.configuration.authentication.Authentication;
import com.hp.autonomy.frontend.configuration.validation.OptionalConfigurationComponent;
import com.hp.autonomy.frontend.find.core.configuration.*;
import com.hp.autonomy.frontend.find.core.configuration.export.ExportConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder(toBuilder = true)
public class MockConfig extends SimpleComponent<MockConfig> implements FindConfig<MockConfig, MockConfig.MockConfigBuilder> {
    SunburstConfiguration sunburst;
    MapConfiguration map;
    SavedSearchConfig savedSearchConfig;
    Integer minScore;
    FieldsInfo fieldsInfo;
    UiCustomization uiCustomization;
    Integer topicMapMaxResults;
    ExportConfig export;
    SearchConfig search;
    UsersConfig users;
    MessageOfTheDayConfig messageOfTheDay;

    @Override
    public Map<String, OptionalConfigurationComponent<?>> getEnabledValidationMap() {
        return null;
    }

    @Override
    public MockConfig withoutDefaultLogin() {
        return null;
    }

    @Override
    public Map<String, OptionalConfigurationComponent<?>> getValidationMap() {
        return null;
    }

    @Override
    public Authentication<?> getAuthentication() {
        return null;
    }

    @Override
    public MockConfig generateDefaultLogin() {
        return null;
    }

    @Override
    public MockConfig withHashedPasswords() {
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static class MockConfigBuilder implements FindConfigBuilder<MockConfig, MockConfigBuilder> {
    }
}
