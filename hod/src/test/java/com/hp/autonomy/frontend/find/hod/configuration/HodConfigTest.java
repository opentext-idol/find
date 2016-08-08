package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.core.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomizationOptionsTest;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HodConfigTest extends ConfigurationComponentTest<HodConfig> {
    @Override
    protected HodConfig constructComponent() {
        return new HodConfig.Builder()
                .setActiveIndexes(Collections.singletonList(ResourceIdentifier.WIKI_CHI))
                .setPublicIndexesEnabled(true)
                .setApiKey(new ApiKey("api-key-abc"))
                .build();
    }

    @Override
    protected Class<HodConfig> getComponentType() {
        return HodConfig.class;
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationOptionsTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/hod/configuration/hod-config.json"));
    }

    @Override
    protected void validateJson(final String json) {
        assertThat(json, containsString("api-key-abc"));
    }

    @Override
    protected void validateParsedComponent(final HodConfig component) {
        assertThat(component.getApiKey(), is(new ApiKey("api-key-123")));
        assertThat(component.getPublicIndexesEnabled(), is(false));
        assertThat(component.getActiveIndexes(), is(empty()));
    }

    @Override
    protected void validateMergedComponent(final HodConfig mergedComponent) {
        assertThat(mergedComponent.getApiKey(), is(new ApiKey("api-key-abc")));
        assertThat(mergedComponent.getPublicIndexesEnabled(), is(true));
        assertThat(mergedComponent.getActiveIndexes(), hasItem(ResourceIdentifier.WIKI_CHI));
    }

    @Override
    public void jsonSymmetry() {
        // Converting an ApiKey to JSON produces {"apiKey": "my-api-key"}, but it should produce "my-api-key"
        // TODO: Remove this override once ISO-51 is complete
    }

    @Test(expected = ConfigException.class)
    public void nullApiKeyInvalid() throws ConfigException {
        new HodConfig.Builder()
                .setApiKey(null)
                .setPublicIndexesEnabled(true)
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullPublicIndexesEnabledInvalid() throws ConfigException {
        new HodConfig.Builder()
                .setApiKey(new ApiKey("my-api-key"))
                .setPublicIndexesEnabled(null)
                .build()
                .basicValidate("configSection");
    }
}
