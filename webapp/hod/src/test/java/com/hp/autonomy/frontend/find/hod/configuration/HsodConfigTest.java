package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.core.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomizationOptionsTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class HsodConfigTest extends ConfigurationComponentTest<HsodConfig> {
    private static final String LANDING_PAGE_URL = "https://search.my-domain.com";
    private static final String JSON_LANDING_PAGE_URL = "https://search.another-domain.com";

    @Override
    protected HsodConfig constructComponent() {
        try {
            return new HsodConfig.Builder()
                    .setLandingPageUrl(new URL(LANDING_PAGE_URL))
                    .build();
        } catch (final MalformedURLException e) {
            throw new AssertionError("Could not parse landing page URL", e);
        }
    }

    @Override
    protected Class<HsodConfig> getComponentType() {
        return HsodConfig.class;
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationOptionsTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/hod/configuration/hsod-config.json"));
    }

    @Override
    protected void validateJson(final String json) {
        assertThat(json, containsString(LANDING_PAGE_URL));
    }

    @Override
    protected void validateParsedComponent(final HsodConfig component) {
        assertThat(component.getLandingPageUrl().toString(), is(JSON_LANDING_PAGE_URL));
    }

    @Override
    protected void validateMergedComponent(final HsodConfig mergedComponent) {
        assertThat(mergedComponent.getLandingPageUrl().toString(), is(LANDING_PAGE_URL));
    }

    @Test(expected = ConfigException.class)
    public void nullLandingPageUrlNotValid() throws ConfigException {
        new HsodConfig.Builder()
                .setLandingPageUrl(null)
                .build()
                .basicValidate("configSection");
    }
}
