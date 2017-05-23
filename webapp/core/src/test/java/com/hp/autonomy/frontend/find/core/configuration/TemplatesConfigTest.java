package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.ConfigurationComponentTest;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(classes = CoreTestContext.class, properties = CORE_CLASSES_PROPERTY)
public class TemplatesConfigTest extends ConfigurationComponentTest<TemplatesConfig>{
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setUp() {
        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }

    @Override
    protected Class<TemplatesConfig> getType() {
        return TemplatesConfig.class;
    }

    @Override
    protected TemplatesConfig constructComponent() {
        final Trigger trigger1 = Trigger.builder()
                .field("some_idol_field")
                .values(Collections.singletonList("some_value"))
                .build();

        final Trigger trigger2 = Trigger.builder()
                .field("some_other_idol_field")
                .values(Arrays.asList("some_value", "some_other_value"))
                .build();

        final Template template = Template.builder()
                .file("some_file.tmpl")
                .triggers(Arrays.asList(trigger1, trigger2))
                .build();

        return TemplatesConfig.builder()
                .searchResultTemplate(template)
                .previewPanelTemplate(template)
                .build();
    }

    @Override
    protected String sampleJson() throws IOException {
        return IOUtils.toString(TemplatesConfigTest.class.getResourceAsStream("/com/hp/autonomy/frontend/find/core/configuration/templates.json"));
    }

    @Override
    protected void validateJson(final JsonContent<TemplatesConfig> jsonContent) {
        jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].file", "some_file.tmpl");
        jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].triggers[0].field", "some_idol_field");
        jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].triggers[0].values[0]", "some_value");
        jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].triggers[1].field", "some_other_idol_field");
        jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].triggers[1].values[0]", "some_value");
        jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].triggers[1].values[1]", "some_other_value");jsonContent.assertThat().hasJsonPathStringValue("@.searchResult[0].file", "some_file.tmpl");
        jsonContent.assertThat().hasJsonPathStringValue("@.previewPanel[0].triggers[0].field", "some_idol_field");
        jsonContent.assertThat().hasJsonPathStringValue("@.previewPanel[0].triggers[0].values[0]", "some_value");
        jsonContent.assertThat().hasJsonPathStringValue("@.previewPanel[0].triggers[1].field", "some_other_idol_field");
        jsonContent.assertThat().hasJsonPathStringValue("@.previewPanel[0].triggers[1].values[0]", "some_value");
        jsonContent.assertThat().hasJsonPathStringValue("@.previewPanel[0].triggers[1].values[1]", "some_other_value");
    }

    @Override
    protected void validateParsedComponent(final ObjectContent<TemplatesConfig> objectContent) {
        assertThat(objectContent.getObject().getSearchResult().get(0).getFile(), is("sometemplate.tmpl"));
        assertThat(objectContent.getObject().getSearchResult().get(0).getTriggers().get(0).getField(), is("some_idol_field"));
        assertThat(objectContent.getObject().getSearchResult().get(0).getTriggers().get(0).getValues(), hasItem("some_value"));
        assertThat(objectContent.getObject().getPreviewPanel().get(0).getFile(), is("sometemplate.tmpl"));
        assertThat(objectContent.getObject().getPreviewPanel().get(0).getTriggers().get(0).getField(), is("some_idol_field"));
        assertThat(objectContent.getObject().getPreviewPanel().get(0).getTriggers().get(0).getValues(), hasItem("some_value"));
    }

    @Override
    protected void validateMergedComponent(final ObjectContent<TemplatesConfig> objectContent) {
        assertThat(objectContent.getObject().getSearchResult(), hasItem(Template.builder()
                .file("some_file.tmpl")
                .triggers(Arrays.asList(Trigger.builder()
                        .field("some_idol_field")
                        .values(Collections.singletonList("some_value"))
                        .build(), Trigger.builder()
                        .field("some_other_idol_field")
                        .values(Arrays.asList("some_value", "some_other_value"))
                        .build()))
                .build()
        ));

        assertThat(objectContent.getObject().getPreviewPanel(), hasItem(Template.builder()
                .file("some_file.tmpl")
                .triggers(Arrays.asList(Trigger.builder()
                        .field("some_idol_field")
                        .values(Collections.singletonList("some_value"))
                        .build(), Trigger.builder()
                        .field("some_other_idol_field")
                        .values(Arrays.asList("some_value", "some_other_value"))
                        .build()))
                .build()
        ));
    }

    @Override
    protected void validateString(final String s) {
        assertTrue(s.contains("searchResult"));
    }

    @Test
    public void listsTemplateFiles() {
        final TemplatesConfig config = TemplatesConfig.builder()
                .promotionTemplate(template("person.html"))
                .searchResultTemplate(template("person.html"))
                .searchResultTemplate(template("document.html"))
                .build();

        assertThat(config.listTemplateFiles(), containsInAnyOrder("person.html", "document.html"));
    }

    private Template template(final String fileName) {
        return Template.builder().file(fileName).build();
    }
}
