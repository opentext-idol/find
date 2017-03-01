package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomizationOptionsTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HsodConfigTest extends ConfigurationComponentTest<HsodConfig> {
    private static final String LANDING_PAGE_URL = "https://search.my-domain.com";
    private static final String JSON_LANDING_PAGE_URL = "https://search.another-domain.com";

    @Override
    protected Class<HsodConfig> getType() {
        return HsodConfig.class;
    }

    @Override
    protected HsodConfig constructComponent() {
        try {
            return HsodConfig.builder()
                    .landingPageUrl(new URL(LANDING_PAGE_URL))
                    .build();
        } catch (final MalformedURLException e) {
            throw new AssertionError("Could not parse landing page URL", e);
        }
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(UiCustomizationOptionsTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/hod/configuration/hsod-config.json"));
    }

    @Override
    protected void validateJson(final JsonContent<HsodConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathStringValue("@.landingPageUrl", LANDING_PAGE_URL);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<HsodConfig> objectContent) {
        assertThat(objectContent.getObject().getLandingPageUrl().toString(), is(JSON_LANDING_PAGE_URL));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<HsodConfig> objectContent) {
        assertThat(objectContent.getObject().getLandingPageUrl().toString(), is(LANDING_PAGE_URL));
    }

    @Override
    protected void validateString(final String s) {
        assertTrue(s.contains("landingPageUrl"));
    }

    @Test(expected = ConfigException.class)
    public void nullLandingPageUrlNotValid() throws ConfigException {
        HsodConfig.builder()
                .landingPageUrl(null)
                .build()
                .basicValidate("configSection");
    }
}
