package com.hp.autonomy.frontend.find.core.configuration.export;

import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExportConfigTest extends ConfigurationComponentTest<ExportConfig> {
    @Override
    protected Class<ExportConfig> getType() {
        return ExportConfig.class;
    }

    @Override
    protected ExportConfig constructComponent() {
        return ExportConfig.builder()
                .powerpoint(PowerPointConfig.builder()
                        .marginLeft(0.1)
                        .marginRight(0.1)
                        .marginTop(0.1)
                        .marginBottom(0.1)
                        .build())
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return "{\"powerpoint\": {}}";
    }

    @Override
    protected void validateJson(final JsonContent<ExportConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathNumberValue("@.powerpoint.marginLeft", 0.1);
        jsonContent.assertThat().hasJsonPathNumberValue("@.powerpoint.marginRight", 0.1);
        jsonContent.assertThat().hasJsonPathNumberValue("@.powerpoint.marginTop", 0.1);
        jsonContent.assertThat().hasJsonPathNumberValue("@.powerpoint.marginBottom", 0.1);
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<ExportConfig> objectContent) {
        final ExportConfig exportConfig = objectContent.getObject();
        assertNotNull(exportConfig);
        final PowerPointConfig powerPointConfig = exportConfig.getPowerpoint();
        assertNotNull(powerPointConfig);
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<ExportConfig> objectContent) {
        final ExportConfig exportConfig = objectContent.getObject();
        assertNotNull(exportConfig);
        final PowerPointConfig powerPointConfig = exportConfig.getPowerpoint();
        assertNotNull(powerPointConfig);
        assertEquals(0.1, powerPointConfig.getMarginLeft(), 0.001);
        assertEquals(0.1, powerPointConfig.getMarginRight(), 0.001);
        assertEquals(0.1, powerPointConfig.getMarginTop(), 0.001);
        assertEquals(0.1, powerPointConfig.getMarginBottom(), 0.001);
    }

    @Override
    protected void validateString(final String s) {
        assertThat(s, containsString("powerpoint"));
    }
}
