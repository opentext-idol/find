package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.core.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomizationOptionsTest;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HodConfigTest extends ConfigurationComponentTest<HodConfig> {
    @Override
    protected HodConfig constructComponent() {
        try {
            return new HodConfig.Builder()
                    .setActiveIndexes(Collections.singletonList(ResourceIdentifier.WIKI_CHI))
                    .setPublicIndexesEnabled(true)
                    .setApiKey(new ApiKey("api-key-abc"))
                    .setSsoPageGetUrl(new URL("https://dev.havenapps.io/sso.html"))
                    .setSsoPagePostUrl(new URL("https://dev.havenapps.io/sso"))
                    .setEndpointUrl(new URL("https://api.int.havenondemand.com"))
                    .build();

        } catch (final MalformedURLException e) {
            throw new AssertionError("Failed to parse URL", e);
        }
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
        assertThat(json, containsString("true"));
        assertThat(json, containsString("https://dev.havenapps.io/sso.html"));
        assertThat(json, containsString("https://api.int.havenondemand.com"));
    }

    @Override
    protected void validateParsedComponent(final HodConfig component) {
        assertThat(component.getApiKey(), is(new ApiKey("api-key-123")));
        assertThat(component.getPublicIndexesEnabled(), is(false));
        assertThat(component.getActiveIndexes(), is(empty()));
        assertThat(component.getSsoPageGetUrl().toString(), is("https://dev.int.havenapps.io/sso.html"));
        assertThat(component.getEndpointUrl(), is(nullValue()));
    }

    @Override
    protected void validateMergedComponent(final HodConfig mergedComponent) {
        assertThat(mergedComponent.getApiKey(), is(new ApiKey("api-key-abc")));
        assertThat(mergedComponent.getPublicIndexesEnabled(), is(true));
        assertThat(mergedComponent.getActiveIndexes(), hasItem(ResourceIdentifier.WIKI_CHI));
        assertThat(mergedComponent.getSsoPageGetUrl().toString(), is("https://dev.havenapps.io/sso.html"));
        assertThat(mergedComponent.getEndpointUrl().toString(), is("https://api.int.havenondemand.com"));
    }

    @Override
    public void jsonSymmetry() {
        // Converting an ApiKey to JSON produces {"apiKey": "my-api-key"}, but it should produce "my-api-key"
        // TODO: Remove this override once ISO-51 is complete
    }

    @Test(expected = ConfigException.class)
    public void nullApiKeyInvalid() throws ConfigException, MalformedURLException {
        new HodConfig.Builder()
                .setApiKey(null)
                .setPublicIndexesEnabled(true)
                .setSsoPageGetUrl(new URL("https://dev.int.havenapps.io/sso.html"))
                .setEndpointUrl(new URL("https://api.int.havenondemand.com"))
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullPublicIndexesEnabledInvalid() throws ConfigException, MalformedURLException {
        new HodConfig.Builder()
                .setApiKey(new ApiKey("my-api-key"))
                .setPublicIndexesEnabled(null)
                .setSsoPageGetUrl(new URL("https://dev.int.havenapps.io/sso.html"))
                .setEndpointUrl(new URL("https://api.int.havenondemand.com"))
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullSsoPageUrlInvalid() throws ConfigException, MalformedURLException {
        new HodConfig.Builder()
                .setApiKey(new ApiKey("my-api-key"))
                .setPublicIndexesEnabled(true)
                .setEndpointUrl(new URL("https://api.int.havenondemand.com"))
                .setSsoPageGetUrl(null)
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullEndpointUrlInvalid() throws ConfigException, MalformedURLException {
        new HodConfig.Builder()
                .setApiKey(new ApiKey("my-api-key"))
                .setPublicIndexesEnabled(true)
                .setSsoPageGetUrl(new URL("https://dev.int.havenapps.io/sso.html"))
                .setEndpointUrl(null)
                .build()
                .basicValidate("configSection");
    }
}
