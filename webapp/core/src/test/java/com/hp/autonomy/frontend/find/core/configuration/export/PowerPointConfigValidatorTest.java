package com.hp.autonomy.frontend.find.core.configuration.export;

import com.hp.autonomy.frontend.configuration.validation.Validator;
import com.hp.autonomy.frontend.reports.powerpoint.PowerPointService;
import com.hp.autonomy.frontend.reports.powerpoint.TemplateLoadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;

@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "unused"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PowerPointConfigValidator.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PowerPointConfigValidatorTest {
    @MockBean
    private PowerPointService powerPointService;
    @Autowired
    private Validator<PowerPointConfig> validator;

    @Test
    public void validConfig() throws IOException {
        final File tmpFile = File.createTempFile("temp", "pptx");
        tmpFile.deleteOnExit();

        assertTrue(validator.validate(PowerPointConfig.builder()
                .templateFile(tmpFile.getAbsolutePath())
                .build())
                .isValid());
    }

    @Test
    public void invalidConfig() {
        assertFalse(validator.validate(PowerPointConfig.builder()
                .templateFile("./bad.pptx")
                .build())
                .isValid());
    }

    @Test
    public void invalidTemplate() throws IOException, TemplateLoadException {
        doThrow(new TemplateLoadException("")).when(powerPointService).validateTemplate();

        final File tmpFile = File.createTempFile("temp", "pptx");
        tmpFile.deleteOnExit();

        assertFalse(validator.validate(PowerPointConfig.builder()
                .templateFile(tmpFile.getAbsolutePath())
                .build())
                .isValid());
    }
}
