package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomizationOptionsTest;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HodConfigTest extends ConfigurationComponentTest<HodConfig> {
    @Override
    protected Class<HodConfig> getType() {
        return HodConfig.class;
    }

    @Override
    protected HodConfig constructComponent() {
        try {
            return HodConfig.builder()
                    .activeIndexes(Collections.singletonList(ResourceIdentifier.WIKI_CHI))
                    .publicIndexesEnabled(true)
                    .apiKey(new ApiKey("api-key-abc"))
                    .ssoPageGetUrl(new URL("https://dev.havenapps.io/sso.html"))
                    .ssoPagePostUrl(new URL("https://dev.havenapps.io/sso"))
                    .endpointUrl(new URL("https://api.int.havenondemand.com"))
                    .build();

        } catch (final MalformedURLException e) {
            throw new AssertionError("Failed to parse URL", e);
        }
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationOptionsTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/hod/configuration/hod-config.json"));
    }

    @Override
    protected void validateJson(final JsonContent<HodConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathStringValue("@.activeIndexes[0].domain", ResourceIdentifier.WIKI_CHI.getDomain());
        jsonContent.assertThat().hasJsonPathStringValue("@.activeIndexes[0].name", ResourceIdentifier.WIKI_CHI.getName());
        jsonContent.assertThat().hasJsonPathBooleanValue("@.publicIndexesEnabled", true);
//        jsonContent.assertThat().hasJsonPathStringValue("@.apiKey", "api-key-abc"); TODO: see other to-do comment below
        jsonContent.assertThat().hasJsonPathStringValue("@.ssoPageGetUrl", "https://dev.havenapps.io/sso.html");
        jsonContent.assertThat().hasJsonPathStringValue("@.ssoPagePostUrl", "https://dev.havenapps.io/sso");
        jsonContent.assertThat().hasJsonPathStringValue("@.endpointUrl", "https://api.int.havenondemand.com");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<HodConfig> objectContent) {
        final HodConfig component = objectContent.getObject();
        assertThat(component.getApiKey(), is(new ApiKey("api-key-123")));
        assertThat(component.getPublicIndexesEnabled(), is(false));
        assertThat(component.getActiveIndexes(), is(empty()));
        assertThat(component.getSsoPageGetUrl().toString(), is("https://dev.int.havenapps.io/sso.html"));
        assertThat(component.getEndpointUrl(), is(nullValue()));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<HodConfig> objectContent) {
        final HodConfig mergedComponent = objectContent.getObject();
        assertThat(mergedComponent.getApiKey(), is(new ApiKey("api-key-abc")));
        assertThat(mergedComponent.getPublicIndexesEnabled(), is(true));
        assertThat(mergedComponent.getActiveIndexes(), hasItem(ResourceIdentifier.WIKI_CHI));
        assertThat(mergedComponent.getSsoPageGetUrl().toString(), is("https://dev.havenapps.io/sso.html"));
        assertThat(mergedComponent.getEndpointUrl().toString(), is("https://api.int.havenondemand.com"));
    }

    @Override
    protected void validateString(final String s) {
        assertTrue(s.contains("activeIndexes"));
    }

    @Override
    public void jsonSymmetry() {
        // Converting an ApiKey to JSON produces {"apiKey": "my-api-key"}, but it should produce "my-api-key"
        // TODO: Remove this override once ISO-51 is complete
    }

    @Test(expected = ConfigException.class)
    public void nullApiKeyInvalid() throws ConfigException, MalformedURLException {
        HodConfig.builder()
                .apiKey(null)
                .publicIndexesEnabled(true)
                .ssoPageGetUrl(new URL("https://dev.int.havenapps.io/sso.html"))
                .endpointUrl(new URL("https://api.int.havenondemand.com"))
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullPublicIndexesEnabledInvalid() throws ConfigException, MalformedURLException {
        HodConfig.builder()
                .apiKey(new ApiKey("my-api-key"))
                .publicIndexesEnabled(null)
                .ssoPageGetUrl(new URL("https://dev.int.havenapps.io/sso.html"))
                .endpointUrl(new URL("https://api.int.havenondemand.com"))
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullSsoPageUrlInvalid() throws ConfigException, MalformedURLException {
        HodConfig.builder()
                .apiKey(new ApiKey("my-api-key"))
                .publicIndexesEnabled(true)
                .endpointUrl(new URL("https://api.int.havenondemand.com"))
                .ssoPageGetUrl(null)
                .build()
                .basicValidate("configSection");
    }

    @Test(expected = ConfigException.class)
    public void nullEndpointUrlInvalid() throws ConfigException, MalformedURLException {
        HodConfig.builder()
                .apiKey(new ApiKey("my-api-key"))
                .publicIndexesEnabled(true)
                .ssoPageGetUrl(new URL("https://dev.int.havenapps.io/sso.html"))
                .endpointUrl(null)
                .build()
                .basicValidate("configSection");
    }
}
